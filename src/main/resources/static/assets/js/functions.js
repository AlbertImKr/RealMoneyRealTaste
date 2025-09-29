/**
 * Social - Network, Community and Event Theme
 *
 * @author Webestica (https://www.webestica.com/)
 * @version 1.1.2
 **/


/* ===================
Table Of Content
======================
01 PRELOADER
02 NAVBAR DROPDOWN HOVER
03 TINY SLIDER
04 TOOLTIP
05 POPOVER
06 VIDEO PLAYER
07 GLIGHTBOX
08 SIDEBAR TOGGLE START
09 SIDEBAR TOGGLE END
10 CHOICES
11 AUTO RESIZE TEXTAREA
12 DROP ZONE
13 FLAT PICKER
14 AVATAR IMAGE
15 CUSTOM SCROLLBAR
16 TOASTS
17 PSWMETER
18 FAKE PASSWORD
====================== */

"use strict";
!function () {

    window.Element.prototype.removeClass = function () {
        let className = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "",
            selectors = this;
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (this.isVariableDefined(selectors) && className) {
            selectors.classList.remove(className);
        }
        return this;
    }
    window.Element.prototype.addClass = function () {
        let className = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "",
            selectors = this;
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (this.isVariableDefined(selectors) && className) {
            selectors.classList.add(className);
        }
        return this;
    }
    window.Element.prototype.toggleClass = function () {
        let className = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "",
            selectors = this;
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (this.isVariableDefined(selectors) && className) {
            selectors.classList.toggle(className);
        }
        return this;
    }
    window.Element.prototype.isVariableDefined = function () {
        return !!this && typeof (this) != 'undefined' && this != null;
    }
}();


const e = {
    init: function () {
        e.preLoader();
        e.navbarDropdownHover();
        e.tinySlider();
        e.toolTipFunc();
        e.popOverFunc();
        e.videoPlyr();
        e.sidebarToggleStart();
        e.sidebarToggleEnd();
        e.autoResize();
        e.DropZone();
        e.flatPicker();
        e.avatarImg();
        e.customScrollbar();
        e.toasts();
        e.pswMeter();
        e.fakePwd();
    },
    isVariableDefined: function (el) {
        return el != null; // null과 undefined 둘 다 체크 (== 사용)
    },
    getParents: function (el, selector, filter) {
        const result = [];
        let currentEl = el.parentElement;

        while (currentEl) {
            // selector가 있고 매칭되면 해당 요소 반환 (단일 결과)
            if (selector && this.matches(currentEl, selector)) {
                return currentEl;
            }

            // filter 조건 체크
            if (this.shouldIncludeParent(currentEl, filter)) {
                result.push(currentEl);
            }

            currentEl = currentEl.parentElement;
        }

        return result;
    },

    shouldIncludeParent: function (el, filter) {
        // filter가 없으면 모든 부모 포함
        if (!filter) {
            return true;
        }

        // filter가 문자열(selector)이면 매칭 체크
        if (typeof filter === 'string') {
            return this.matches(el, filter);
        }

        // filter가 함수면 실행
        if (typeof filter === 'function') {
            return filter(el);
        }

        return false;
    },

    matches: function (el, selector) {
        if (!el || !selector) return false;

        const matchesSelector = el.matches || el.webkitMatchesSelector ||
            el.mozMatchesSelector || el.msMatchesSelector;

        return matchesSelector.call(el, selector);
    },
    getNextSiblings: function (el, selector, filter) {
        const sibs = [];
        let currentElem = el.nextElementSibling;

        while (currentElem) {
            if (this.shouldIncludeSibling(currentElem, selector, filter)) {
                if (selector) {
                    return currentElem; // 첫 번째 매칭 요소 반환
                }
                sibs.push(currentElem);
            }
            currentElem = currentElem.nextElementSibling;
        }

        return sibs;
    },
    shouldIncludeSibling: function (elem, selector, filter) {
        // filter 체크
        if (filter && !filter(elem)) {
            return false;
        }

        // selector 체크
        if (selector) {
            const matchesSelector = elem.matches || elem.webkitMatchesSelector ||
                elem.mozMatchesSelector || elem.msMatchesSelector;
            return matchesSelector.call(elem, selector);
        }

        return true;
    },
    on: function (selectors, type, listener) {
        document.addEventListener("DOMContentLoaded", () => {
            if (!(selectors instanceof HTMLElement) && selectors !== null) {
                selectors = document.querySelector(selectors);
            }
            selectors.addEventListener(type, listener);
        });
    },
    onAll: function (selectors, type, listener) {
        document.addEventListener("DOMContentLoaded", () => {
            document.querySelectorAll(selectors).forEach((element) => {
                if (type.indexOf(',') > -1) {
                    let types = type.split(',');
                    types.forEach((type) => {
                        element.addEventListener(type, listener);
                    });
                } else {
                    element.addEventListener(type, listener);
                }


            });
        });
    },
    removeClass: function (selectors, className) {
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (e.isVariableDefined(selectors)) {
            selectors.removeClass(className);
        }
    },
    removeAllClass: function (selectors, className) {
        if (e.isVariableDefined(selectors) && (selectors instanceof HTMLElement)) {
            document.querySelectorAll(selectors).forEach((element) => {
                element.removeClass(className);
            });
        }

    },
    toggleClass: function (selectors, className) {
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (e.isVariableDefined(selectors)) {
            selectors.toggleClass(className);
        }
    },
    addClass: function (selectors, className) {
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (e.isVariableDefined(selectors)) {
            selectors.addClass(className);
        }
    },
    select: function (selectors) {
        return document.querySelector(selectors);
    },
    selectAll: function (selectors) {
        return document.querySelectorAll(selectors);
    },

    // START: 01 Preloader
    preLoader: function () {
        window.onload = function () {
            let preloader = e.select('.preloader');
            if (e.isVariableDefined(preloader)) {
                preloader.className += ' animate__animated animate__fadeOut';
                setTimeout(function () {
                    preloader.style.display = 'none';
                }, 200);
            }
        };
    },
    // END: Preloader

    // START: 02 Navbar dropdown hover
    navbarDropdownHover: function () {
        e.onAll('.dropdown-menu a.dropdown-item.dropdown-toggle', 'click', function (event) {
            let element = this;
            event.preventDefault();
            event.stopImmediatePropagation();
            if (e.isVariableDefined(element.nextElementSibling) && !element.nextElementSibling.classList.contains("show")) {
                const parents = e.getParents(element, '.dropdown-menu');
                e.removeClass(parents.querySelector('.show'), "show");
                if (e.isVariableDefined(parents.querySelector('.dropdown-opened'))) {
                    e.removeClass(parents.querySelector('.dropdown-opened'), "dropdown-opened");
                }
            }
            let $subMenu = e.getNextSiblings(element, ".dropdown-menu");
            e.toggleClass($subMenu, "show");
            $subMenu.previousElementSibling.toggleClass('dropdown-opened');
            let parents = e.getParents(element, 'li.nav-item.dropdown.show');
            if (e.isVariableDefined(parents) && parents.length > 0) {
                e.on(parents, 'hidden.bs.dropdown', function () {
                    e.removeAllClass('.dropdown-submenu .show');
                });
            }
        });
    },
    // END: Navbar dropdown hover

    // START: 03 Tiny Slider
    tinySlider: function () {
        let $carousel = e.select('.tiny-slider-inner');
        if (e.isVariableDefined($carousel)) {
            let tnsCarousel = e.selectAll('.tiny-slider-inner');
            tnsCarousel.forEach(slider => {
                let slider1 = slider;
                let sliderMode = slider1.getAttribute('data-mode') ? slider1.getAttribute('data-mode') : 'carousel';
                let sliderAxis = slider1.getAttribute('data-axis') ? slider1.getAttribute('data-axis') : 'horizontal';
                let sliderSpace = slider1.getAttribute('data-gutter') ? slider1.getAttribute('data-gutter') : 30;
                let sliderEdge = slider1.getAttribute('data-edge') ? slider1.getAttribute('data-edge') : 0;

                let sliderItems = slider1.getAttribute('data-items') ? slider1.getAttribute('data-items') : 4; //option: number (items in all device)
                let sliderItemsXl = slider1.getAttribute('data-items-xl') ? slider1.getAttribute('data-items-xl') : Number(sliderItems); //option: number (items in 1200 to end )
                let sliderItemsLg = slider1.getAttribute('data-items-lg') ? slider1.getAttribute('data-items-lg') : Number(sliderItemsXl); //option: number (items in 992 to 1199 )
                let sliderItemsMd = slider1.getAttribute('data-items-md') ? slider1.getAttribute('data-items-md') : Number(sliderItemsLg); //option: number (items in 768 to 991 )
                let sliderItemsSm = slider1.getAttribute('data-items-sm') ? slider1.getAttribute('data-items-sm') : Number(sliderItemsMd); //option: number (items in 576 to 767 )
                let sliderItemsXs = slider1.getAttribute('data-items-xs') ? slider1.getAttribute('data-items-xs') : Number(sliderItemsSm); //option: number (items in start to 575 )

                let sliderSpeed = slider1.getAttribute('data-speed') ? slider1.getAttribute('data-speed') : 500;
                let sliderautoWidth = slider1.getAttribute('data-autowidth') === 'true'; //option: true or false
                let sliderArrow = slider1.getAttribute('data-arrow') !== 'false'; //option: true or false
                let sliderDots = slider1.getAttribute('data-dots') !== 'false'; //option: true or false

                let sliderAutoPlay = slider1.getAttribute('data-autoplay') !== 'false'; //option: true or false
                let sliderAutoPlayTime = slider1.getAttribute('data-autoplaytime') ? slider1.getAttribute('data-autoplaytime') : 4000;
                let sliderHoverPause = slider1.getAttribute('data-hoverpause') === 'true'; //option: true or false
                let sliderNavContainer = e.isVariableDefined(e.select('.custom-thumb')) ? e.select('.custom-thumb') : false;
                let sliderLoop = slider1.getAttribute('data-loop') !== 'false'; //option: true or false
                let sliderRewind = slider1.getAttribute('data-rewind') === 'true'; //option: true or false
                let sliderAutoHeight = slider1.getAttribute('data-autoheight') === 'true'; //option: true or false
                let sliderfixedWidth = slider1.getAttribute('data-fixedwidth') === 'true'; //option: true or false
                let sliderTouch = slider1.getAttribute('data-touch') !== 'false'; //option: true or false
                let sliderDrag = slider1.getAttribute('data-drag') !== 'false'; //option: true or false
                // Check if document DIR is RTL
                let ifRtl = document.getElementsByTagName("html")[0].getAttribute("dir");
                let sliderDirection;
                if (ifRtl === 'rtl') {
                    sliderDirection = 'rtl';
                }
                tns({
                    container: slider,
                    mode: sliderMode,
                    axis: sliderAxis,
                    gutter: sliderSpace,
                    edgePadding: sliderEdge,
                    speed: sliderSpeed,
                    autoWidth: sliderautoWidth,
                    controls: sliderArrow,
                    nav: sliderDots,
                    autoplay: sliderAutoPlay,
                    autoplayTimeout: sliderAutoPlayTime,
                    autoplayHoverPause: sliderHoverPause,
                    autoplayButton: false,
                    autoplayButtonOutput: false,
                    controlsPosition: top,
                    navContainer: sliderNavContainer,
                    navPosition: top,
                    autoplayPosition: top,
                    controlsText: [
                        '<i class="fa-solid fa-chevron-left"></i>',
                        '<i class="fa-solid fa-chevron-right"></i>'
                    ],
                    loop: sliderLoop,
                    rewind: sliderRewind,
                    autoHeight: sliderAutoHeight,
                    fixedWidth: sliderfixedWidth,
                    touch: sliderTouch,
                    mouseDrag: sliderDrag,
                    arrowKeys: true,
                    items: sliderItems,
                    textDirection: sliderDirection,
                    lazyload: true,
                    lazyloadSelector: '.lazy',
                    responsive: {
                        0: {
                            items: Number(sliderItemsXs)
                        },
                        576: {
                            items: Number(sliderItemsSm)
                        },
                        768: {
                            items: Number(sliderItemsMd)
                        },
                        992: {
                            items: Number(sliderItemsLg)
                        },
                        1200: {
                            items: Number(sliderItemsXl)
                        }
                    }
                });
            });
        }
    },
    // END: Tiny Slider


    // START: 04 Tooltip
    // Enable tooltips everywhere via data-toggle attribute
    toolTipFunc: function () {
        let tooltipTriggerList = [].slice.call(e.selectAll('[data-bs-toggle="tooltip"]'))
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl)
        });
    },
    // END: Tooltip

    // START: 05 Popover
    // Enable popover everywhere via data-toggle attribute
    popOverFunc: function () {
        let popoverTriggerList = [].slice.call(e.selectAll('[data-bs-toggle="popover"]'))
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl)
        });
    },
    // END: Popover

    // START: 06 Video player
    videoPlyr: function () {
        let vdp = e.select('.player-wrapper');
        if (e.isVariableDefined(vdp)) {
            // youtube
            window.player = Plyr.setup('.player-youtube', {});

            // Vimeo
            window.player = Plyr.setup('.player-vimeo', {});

            // HTML video
            window.player = Plyr.setup('.player-html', {
                captions: {active: true}
            });

            // HTML audio
            window.player = Plyr.setup('.player-audio', {});
        }
    },
    // END: Video player

    // START: 07 GLightbox
    // END: GLightbox

    // START: 08 Sidebar Toggle start
    sidebarToggleStart: function () {
        let sidebar = e.select('.sidebar-start-toggle');
        if (e.isVariableDefined(sidebar)) {
            let sb = e.select('.sidebar-start-toggle');
            let mode = document.getElementsByTagName("BODY")[0];
            sb.addEventListener("click", function () {
                mode.classList.toggle("sidebar-start-enabled");
            });
        }
    },
    // END: Sidebar Toggle

    // START: 09 Sidebar Toggle end
    sidebarToggleEnd: function () {
        let sidebar = e.select('.sidebar-end-toggle');
        if (e.isVariableDefined(sidebar)) {
            let sb = e.select('.sidebar-end-toggle');
            let mode = document.getElementsByTagName("BODY")[0];
            sb.addEventListener("click", function () {
                mode.classList.toggle("sidebar-end-enabled");
            });
        }
    },
    // END: Sidebar Toggle end

    // START: 10 Choices
    // END: Choices

    // START: 11 Auto resize textarea
    autoResize: function () {
        e.selectAll('[data-autoresize]').forEach(function (element) {
            let offset = element.offsetHeight - element.clientHeight;
            element.addEventListener('input', function (event) {
                event.target.style.height = 'auto';
                event.target.style.height = event.target.scrollHeight + offset + 'px';
            });
        });
    },
    // END: Auto resize textarea

    // START: 12 Drop Zone
    DropZone: function () {
        if (e.isVariableDefined(e.select("[data-dropzone]"))) {
            window.Dropzone.autoDiscover = false;

            // 1. Default Dropzone Initialization

            // 2. Custom cover and list previews Dropzone Initialization
            if (e.isVariableDefined(e.select(".dropzone-custom"))) {
                e.selectAll(".dropzone-custom").forEach((d => {
                    d.querySelector(".dz-preview").innerHTML = '';
                }));
            }
        }
    },
    // END: Drop Zone

    // START: 13 Flat picker
    flatPicker: function () {

        let picker = e.select('.flatpickr');
        if (e.isVariableDefined(picker)) {
            let element = e.selectAll('.flatpickr');
            element.forEach(function (item) {
                const dataMode = item.getAttribute('data-mode');
                const mode = ['multiple', 'range'].includes(dataMode) ? dataMode : 'single';
                let enableTime = item.getAttribute('data-enableTime') === 'true'
                let noCalendar = item.getAttribute('data-noCalendar') === 'true'
                let inline = item.getAttribute('data-inline') === 'true'

                flatpickr(item, {
                    mode: mode,
                    enableTime: enableTime,
                    noCalendar: noCalendar,
                    inline: inline,
                    animate: "false",
                    position: "top",
                    dateFormat: "D-m-Y", //Check supported characters here: https://flatpickr.js.org/formatting/
                    disableMobile: "true"
                });

            });
        }
    },
    // END: Flat picker

    // START: 14 Avatar Image
    avatarImg: function () {
        if (e.isVariableDefined(e.select('#avatarUpload'))) {

            let avtInput = e.select('#avatarUpload'),
                avtReset = e.select("#avatar-reset-img"),
                avtPreview = e.select('#avatar-preview');

            // Avatar upload and replace
            avtInput.addEventListener('change', readURL, true);

            function readURL() {
                const file = avtInput.files[0];
                const files = avtInput.files;
                const reader = new FileReader();
                reader.onloadend = function () {
                    avtPreview.src = reader.result;
                }

                if (file && files) {
                    reader.readAsDataURL(file);
                }

                avtInput.value = '';
            }

            // Avatar remove functionality
            avtReset.addEventListener("click", function () {
                avtPreview.src = "/assets/images/avatar/placeholder.jpg";
            });
        }
    },
    // END: Avatar Image

    // START: 15 Custom Scrollbar
    customScrollbar: function () {

        if (e.isVariableDefined(e.select(".custom-scrollbar"))) {
            document.addEventListener("DOMContentLoaded", function () {
                OverlayScrollbars(e.selectAll('.custom-scrollbar'), {
                    resize: "none",
                    scrollbars: {
                        autoHide: 'leave',
                        autoHideDelay: 200
                    },
                    overflowBehavior: {
                        x: "visible-hidden",
                        y: "scroll"
                    }
                });
            });
        }

        if (e.isVariableDefined(e.select(".custom-scrollbar-y"))) {
            document.addEventListener("DOMContentLoaded", function () {
                OverlayScrollbars(e.selectAll('.custom-scrollbar-y'), {
                    resize: "none",
                    scrollbars: {
                        autoHide: 'leave',
                        autoHideDelay: 200
                    },
                    overflowBehavior: {
                        x: "scroll",
                        y: "scroll"
                    }
                });
            });
        }
    },
    // END: Custom Scrollbar

    // START: 16 Toasts
    toasts: function () {
        if (e.isVariableDefined(e.select('.toast-btn'))) {
            window.addEventListener('DOMContentLoaded', () => {
                e.selectAll(".toast-btn").forEach((t) => {
                    t.addEventListener("click", function () {
                        let toastTarget = document.getElementById(t.dataset.target);
                        let toast = new bootstrap.Toast(toastTarget);
                        toast.show();
                    });
                });
            });
        }
    },
    // END: Toasts

    // START: 17 pswMeter
    pswMeter: function () {
        if (e.isVariableDefined(e.select('#pswmeter'))) {
            passwordStrengthMeter({
                containerElement: '#pswmeter',
                passwordInput: '#psw-input',
                showMessage: true,
                messageContainer: '#pswmeter-message',
                messagesList: [
                    'Write your password...',
                    'Easy peasy!',
                    'That is a simple one',
                    'That is better',
                    'Yeah! that password rocks ;)'
                ],
                height: 8,
                borderRadius: 4,
                pswMinLength: 8,
                colorScore1: '#dc3545',
                colorScore2: '#f7c32e',
                colorScore3: '#4f9ef8',
                colorScore4: '#0cbc87'
            });
        }
    },
    // END: pswMeter

    // START: 18 Fake Password
    fakePwd: function () {
        if (e.isVariableDefined(e.select('.fakepassword'))) {
            const password = e.select('.fakepassword');
            const toggler = e.select('.fakepasswordicon');

            const showHidePassword = () => {
                if (password.type === 'password') {
                    password.setAttribute('type', 'text');
                    toggler.classList.add('fa-eye');
                } else {
                    toggler.classList.remove('fa-eye');
                    password.setAttribute('type', 'password');
                }
            };

            toggler.addEventListener('click', showHidePassword);
        }
    }
    // END: Fake Password

};
e.init();
