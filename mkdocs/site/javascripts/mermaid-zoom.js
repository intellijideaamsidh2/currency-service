// Enable pan/zoom for all Mermaid-rendered SVGs after page load and navigation events
(function () {
    function initZoom() {
        try {
            var svgs = document.querySelectorAll('.mermaid svg');
            if (!svgs || svgs.length === 0) return;
            svgs.forEach(function (svg) {
                // Avoid double-initialization
                if (svg.__panzoomInitialized) return;
                svg.__panzoomInitialized = true;
                var container = svg.parentElement;
                // Ensure the container allows overflow for panning
                if (container) {
                    container.style.overflow = 'hidden';
                    container.style.display = 'block';
                }
                // Initialize svg-pan-zoom with sensible defaults
                if (window.svgPanZoom) {
                    window.svgPanZoom(svg, {
                        zoomEnabled: true,
                        controlIconsEnabled: true,
                        fit: true,
                        center: true,
                        minZoom: 0.5,
                        maxZoom: 10,
                        zoomScaleSensitivity: 0.2,
                        contain: true,
                    });
                }
            });
        } catch (e) {
            console.warn('Mermaid zoom init failed:', e);
        }
    }

    // Run on initial load
    document.addEventListener('DOMContentLoaded', initZoom);

    // Re-run on MkDocs Material page changes (instant loading)
    document.addEventListener('readystatechange', function () {
        if (document.readyState === 'complete') initZoom();
    });

    // Support for navigation events in Material (when enabled)
    if (typeof window !== 'undefined') {
        window.addEventListener('hashchange', function () {
            setTimeout(initZoom, 150);
        });
    }

    // Hook into Mermaid rendering if available to reinit per render
    if (window.mermaid) {
        try {
            var origInit = window.mermaid.initialize;
            window.mermaid.initialize = function (config) {
                var result = origInit ? origInit.call(window.mermaid, config) : undefined;
                // Give mermaid time to render before attaching zoom
                setTimeout(initZoom, 300);
                return result;
            };
        } catch (e) {
            // no-op
        }
    }
})();
