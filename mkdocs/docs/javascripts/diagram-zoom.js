// Enable pan/zoom for all Mermaid-rendered SVGs after page load and navigation events
(function () {
    // Toggle interactive controls; when false, no +/- icons, no pan/zoom, no fullscreen
    const INTERACTIVE_DIAGRAMS = false;
    function fitToContainer(svg, panzoom) {
        try {
            const container = svg.parentElement;
            if (!container || !panzoom) return;
            const cw = container.clientWidth || 0;
            const ch = container.clientHeight || 0;
            if (!cw || !ch) { panzoom.fit(); panzoom.center(); return; }
            // Prefer inner <g> bbox for Mermaid output
            const inner = svg.querySelector('g');
            const bbox = (inner && inner.getBBox) ? inner.getBBox() : svg.getBBox();
            if (!bbox || !isFinite(bbox.width) || !isFinite(bbox.height) || bbox.width === 0 || bbox.height === 0) {
                panzoom.fit(); panzoom.center(); return;
            }
            // Reset, then zoom to min scale that fits both width and height
            panzoom.resetZoom();
            panzoom.resetPan();
            const scale = Math.max(0.01, Math.min(cw / bbox.width, ch / bbox.height) * 0.98);
            panzoom.zoom(scale, { x: cw / 2, y: ch / 2 });
            panzoom.center();
        } catch (e) {
            try { panzoom.fit(); panzoom.center(); } catch (_) { }
        }
    }

    function initOne(svg) {
        if (!window.svgPanZoom) return;
        if (svg.__panzoomInitialized) return;
        svg.__panzoomInitialized = true;
        const container = svg.parentElement;
        if (container) container.style.display = 'block';
        const panzoom = window.svgPanZoom(svg, {
            // start enabled to allow programmatic fit, we'll disable if needed
            zoomEnabled: true,
            panEnabled: true,
            controlIconsEnabled: INTERACTIVE_DIAGRAMS,
            fit: true,
            center: true,
            minZoom: 0.1,
            maxZoom: 20,
            zoomScaleSensitivity: 0.15,
            contain: false
        });
        svg.__panzoom = panzoom;
        // Defer fit until container has layout
        (function attemptFit(attempts) {
            attempts = attempts || 0;
            const cw = (container && container.clientWidth) || 0;
            const ch = (container && container.clientHeight) || 0;
            const visible = cw > 0 && ch > 0 && container.offsetParent !== null;
            if (!visible && attempts < 12) return setTimeout(function () { attemptFit(attempts + 1); }, 120);
            fitToContainer(svg, panzoom);
            if (!INTERACTIVE_DIAGRAMS) {
                try { panzoom.disableZoom(); } catch (_) { }
                try { panzoom.disablePan(); } catch (_) { }
                // remove any controls injected by lib just in case
                try { (container.querySelectorAll('[class^="svg-pan-zoom"], .svg-pan-zoom-control') || []).forEach(function (el) { el.remove(); }); } catch (_) { }
            }
        })(0);

        // Add a fullscreen button once per container
        if (INTERACTIVE_DIAGRAMS && container && !container.querySelector('.diagram-fullscreen-btn')) {
            const btn = document.createElement('button');
            btn.className = 'diagram-fullscreen-btn';
            btn.title = 'View fullscreen';
            btn.textContent = '⤢';
            Object.assign(btn.style, {
                position: 'absolute', top: '8px', right: '8px', zIndex: 2,
                border: '1px solid rgba(0,0,0,.2)', borderRadius: '4px',
                background: '#fff', padding: '2px 6px', cursor: 'pointer'
            });
            btn.addEventListener('click', function () {
                const modal = document.createElement('div');
                Object.assign(modal.style, {
                    position: 'fixed', left: 0, top: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.7)', zIndex: 9999,
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                });
                const frame = document.createElement('div');
                Object.assign(frame.style, {
                    background: '#fff', maxWidth: '95vw', maxHeight: '95vh',
                    width: '95vw', height: '95vh', overflow: 'auto', padding: '8px', borderRadius: '6px'
                });
                const clone = container.cloneNode(true);
                const nestedBtn = clone.querySelector('.diagram-fullscreen-btn');
                if (nestedBtn) nestedBtn.remove();
                frame.appendChild(clone);
                modal.appendChild(frame);
                modal.addEventListener('click', function (e) { if (e.target === modal) document.body.removeChild(modal); });
                document.body.appendChild(modal);
                setTimeout(function () {
                    const innerSvg = modal.querySelector('.mermaid svg');
                    if (innerSvg && window.svgPanZoom) {
                        try {
                            const pz = window.svgPanZoom(innerSvg, { controlIconsEnabled: true, contain: false, zoomScaleSensitivity: 0.15, minZoom: 0.05, maxZoom: 30 });
                            fitToContainer(innerSvg, pz);
                        } catch (e) { console.debug('modal zoom init failed', e); }
                    }
                }, 50);
            });
            container.appendChild(btn);
        }
    }

    function initZoom() {
        try {
            var svgs = document.querySelectorAll('.mermaid svg');
            if (!svgs || svgs.length === 0) return;
            svgs.forEach(initOne);
        } catch (e) {
            console.warn('Diagram zoom init failed:', e);
        }
    }

    document.addEventListener('DOMContentLoaded', initZoom);
    document.addEventListener('readystatechange', function () { if (document.readyState === 'complete') initZoom(); });
    if (typeof window !== 'undefined') {
        window.addEventListener('hashchange', function () { setTimeout(initZoom, 150); });
        window.addEventListener('resize', function () {
            setTimeout(function () {
                try { document.querySelectorAll('.mermaid svg').forEach(function (svg) { if (svg.__panzoom) fitToContainer(svg, svg.__panzoom); }); } catch (_) { }
            }, 150);
        });
    }
    // Observe DOM changes for dynamically rendered content
    (function () {
        const target = document.querySelector('.md-content');
        if (!target || !('MutationObserver' in window)) return;
        const obs = new MutationObserver(function (mutations) {
            for (const m of mutations) { if (m.addedNodes && m.addedNodes.length) { setTimeout(initZoom, 100); break; } }
        });
        obs.observe(target, { childList: true, subtree: true });
    })();

    // If Mermaid re-initializes, schedule a zoom init pass
    if (window.mermaid) {
        try {
            const origInit = window.mermaid.initialize;
            window.mermaid.initialize = function (config) {
                const result = origInit ? origInit.call(window.mermaid, config) : undefined;
                setTimeout(initZoom, 300);
                return result;
            };
        } catch (e) { console.debug('Mermaid init hook failed:', e); }
    }
})();
