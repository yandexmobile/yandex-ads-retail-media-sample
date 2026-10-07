import UIKit
@_spi(RetailMedia) import YandexMobileAds

/// The Home display slider: one `readyResponse` may hold several creatives, all of them are shown as pages with
/// the neighbours peeking in at the edges.
///
/// Auto-scroll runs only while the slider is on screen and the app is in the foreground — a slide scrolled in
/// off-screen would still be laid out and tracked. A drag pauses it, a single creative gets no timer, and the
/// wrap from the last slide to the first is a jump: an animated scroll back would show every slide in between.
final class AdSliderCell: UICollectionViewCell, UIScrollViewDelegate {

    static let reuseIdentifier = "AdSliderCell"

    private static let sideInset: CGFloat = 16
    private static let spacing: CGFloat = 8
    private static let dotsHeight: CGFloat = 20
    private static let autoScrollInterval: TimeInterval = 3

    static func height(for ads: RetailMediaAds, width: CGFloat) -> CGFloat {
        let slideWidth = width - sideInset * 2
        let tallest = ads.ads.map { slideWidth / $0.mediaAspectRatio }.max() ?? 0
        return tallest + (ads.ads.count > 1 ? dotsHeight : 0)
    }

    var isOnScreen = false {
        didSet { updateAutoScroll() }
    }

    private let container = SliderContainerView()
    private let scrollView = UIScrollView()
    private let slidesStack = UIStackView()
    private let pageControl = UIPageControl()
    private var dotsHeightConstraint: NSLayoutConstraint?

    private var shownRetailMedia: RetailMedia?
    private var slides: [AdSlideView] = []
    private var timer: Timer?
    private var isDragging = false
    private var isInForeground = true

    override init(frame: CGRect) {
        super.init(frame: frame)
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // [Step 4]
    /// The slot is bound to the slider itself and each creative to its own slide. `configure` runs again whenever
    /// the grid reconfigures the slider, so the same ads are not rebound: that would restart impression tracking
    /// and reset the position.
    func configure(with ads: RetailMediaAds) {
        guard !isSameRetailMedia(ads.retailMedia, shownRetailMedia) else { return }
        shownRetailMedia = ads.retailMedia
        slides.forEach { $0.removeFromSuperview() }
        slides = []
        scrollView.contentOffset = .zero

        do {
            let binder = try ads.retailMedia.bind(to: container)
            for ad in ads.ads {
                let slide = AdSlideView()
                slide.translatesAutoresizingMaskIntoConstraints = false
                slidesStack.addArrangedSubview(slide)
                slide.widthAnchor.constraint(
                    equalTo: scrollView.frameLayoutGuide.widthAnchor,
                    constant: -Self.spacing
                ).isActive = true
                do {
                    try slide.bind(ad, using: binder)
                    slides.append(slide)
                } catch {
                    slide.removeFromSuperview()
                    logAdBindingFailure("Slide", error)
                }
            }
        } catch {
            logAdBindingFailure("Slider", error)
        }

        pageControl.numberOfPages = slides.count
        pageControl.currentPage = 0
        dotsHeightConstraint?.constant = slides.count > 1 ? Self.dotsHeight : 0
        restartAutoScroll()
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        updateAutoScroll()
    }

    // MARK: UIScrollViewDelegate

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        pageControl.currentPage = currentPage
    }

    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        isDragging = true
        updateAutoScroll()
    }

    func scrollViewDidEndDragging(_ scrollView: UIScrollView, willDecelerate decelerate: Bool) {
        guard !decelerate else { return }
        isDragging = false
        updateAutoScroll()
    }

    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        isDragging = false
        updateAutoScroll()
    }

    // MARK: Auto-scroll

    private var currentPage: Int {
        let pageWidth = scrollView.bounds.width
        guard pageWidth > 0 else { return 0 }
        return Int((scrollView.contentOffset.x / pageWidth).rounded())
    }

    private func restartAutoScroll() {
        timer?.invalidate()
        timer = nil
        updateAutoScroll()
    }

    private func updateAutoScroll() {
        let shouldRun = isOnScreen && window != nil && isInForeground && !isDragging && slides.count > 1
        if shouldRun, timer == nil {
            let timer = Timer(
                timeInterval: Self.autoScrollInterval,
                target: self,
                selector: #selector(showNextSlide),
                userInfo: nil,
                repeats: true
            )
            RunLoop.main.add(timer, forMode: .common)
            self.timer = timer
        } else if !shouldRun {
            timer?.invalidate()
            timer = nil
        }
    }

    @objc private func showNextSlide() {
        let next = currentPage + 1
        if next < slides.count {
            scrollView.setContentOffset(CGPoint(x: CGFloat(next) * scrollView.bounds.width, y: 0), animated: true)
        } else {
            scrollView.setContentOffset(.zero, animated: false)
        }
    }

    @objc private func appDidEnterBackground() {
        isInForeground = false
        updateAutoScroll()
    }

    @objc private func appWillEnterForeground() {
        isInForeground = true
        updateAutoScroll()
    }

    // MARK: Layout

    private func setUp() {
        contentView.clipsToBounds = true

        scrollView.isPagingEnabled = true
        scrollView.clipsToBounds = false
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.delegate = self
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        container.scrollView = scrollView

        slidesStack.axis = .horizontal
        slidesStack.alignment = .top
        slidesStack.spacing = Self.spacing
        slidesStack.translatesAutoresizingMaskIntoConstraints = false

        pageControl.isUserInteractionEnabled = false
        pageControl.hidesForSinglePage = true
        pageControl.pageIndicatorTintColor = UIColor(white: 0xCF / 255, alpha: 1)
        pageControl.currentPageIndicatorTintColor = UIColor(white: 0x61 / 255, alpha: 1)
        pageControl.translatesAutoresizingMaskIntoConstraints = false

        container.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(container)
        container.addSubview(scrollView)
        container.addSubview(pageControl)
        scrollView.addSubview(slidesStack)

        let dotsHeightConstraint = pageControl.heightAnchor.constraint(equalToConstant: 0)
        self.dotsHeightConstraint = dotsHeightConstraint

        NSLayoutConstraint.activate([
            container.topAnchor.constraint(equalTo: contentView.topAnchor),
            container.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            container.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            container.bottomAnchor.constraint(equalTo: contentView.bottomAnchor),

            scrollView.topAnchor.constraint(equalTo: container.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: Self.sideInset),
            scrollView.widthAnchor.constraint(
                equalTo: container.widthAnchor,
                constant: Self.spacing - Self.sideInset * 2
            ),
            scrollView.bottomAnchor.constraint(equalTo: pageControl.topAnchor),

            pageControl.leadingAnchor.constraint(equalTo: container.leadingAnchor),
            pageControl.trailingAnchor.constraint(equalTo: container.trailingAnchor),
            pageControl.bottomAnchor.constraint(equalTo: container.bottomAnchor),
            dotsHeightConstraint,

            slidesStack.topAnchor.constraint(equalTo: scrollView.contentLayoutGuide.topAnchor),
            slidesStack.leadingAnchor.constraint(equalTo: scrollView.contentLayoutGuide.leadingAnchor),
            slidesStack.trailingAnchor.constraint(
                equalTo: scrollView.contentLayoutGuide.trailingAnchor,
                constant: -Self.spacing
            ),
            slidesStack.bottomAnchor.constraint(equalTo: scrollView.contentLayoutGuide.bottomAnchor),
            slidesStack.heightAnchor.constraint(equalTo: scrollView.frameLayoutGuide.heightAnchor),
        ])

        let center = NotificationCenter.default
        center.addObserver(
            self,
            selector: #selector(appDidEnterBackground),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )
        center.addObserver(
            self,
            selector: #selector(appWillEnterForeground),
            name: UIApplication.willEnterForegroundNotification,
            object: nil
        )
    }
}

/// The scroll view is one page wide so that paging stops on every slide; the peeking neighbours lie outside its
/// bounds, so touches there are routed to it as well.
private final class SliderContainerView: RetailMediaAdView {

    weak var scrollView: UIScrollView?

    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let hit = super.hitTest(point, with: event)
        guard let scrollView, self.point(inside: point, with: event) else { return hit }
        if let hit, hit.isDescendant(of: scrollView) {
            return hit
        }
        return scrollView
    }
}
