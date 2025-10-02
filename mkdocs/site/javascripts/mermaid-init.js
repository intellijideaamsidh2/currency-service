// Initialize Mermaid on page load for all blocks with class="mermaid"
(function () {
    function init() {
        if (!window.mermaid) return;
        try {
            window.mermaid.initialize({ startOnLoad: true, securityLevel: 'loose', theme: 'neutral', flowchart: { curve: 'linear' } });
            // If MkDocs Material delayed load, retry shortly
            setTimeout(function () {
                try { window.mermaid.contentLoaded(); } catch (e) { }
            }, 100);
        } catch (e) {
            console.warn('Mermaid init error', e);
        }
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
