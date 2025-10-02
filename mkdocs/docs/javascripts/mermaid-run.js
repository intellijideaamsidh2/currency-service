// Render Mermaid diagrams from code/pre blocks and from fenced ```mermaid blocks
(function () {
    var isRendering = false;
    var lastRender = 0;

    function isMermaidCode(code) {
        if (!code) return false;
        if (code.classList && code.classList.contains('language-mermaid')) return true;
        var txt = (code.textContent || '').trim();
        // Support a broad set of Mermaid diagram starters
        return /^(graph|flowchart|sequenceDiagram|classDiagram|erDiagram|gantt|stateDiagram|stateDiagram-v2|mindmap|pie|journey|timeline)/.test(txt);
    }

    function renderOne(code) {
        var pre = code && code.parentElement;
        if (!pre || pre.dataset.mmdProcessed === '1') return;
        if (!isMermaidCode(code)) return;
        pre.dataset.mmdProcessed = '1';
        var src = code.textContent || '';
        try {
            var id = 'mmd-' + Math.random().toString(36).slice(2);
            window.mermaid.render(id, src, function (svgCode) {
                try {
                    var div = document.createElement('div');
                    var extra = Array.from(code.classList || []).filter(function (c) { return c !== 'language-mermaid' && c !== 'mermaid' && !c.startsWith('language-'); }).join(' ');
                    div.className = ('mermaid ' + extra).trim();
                    div.innerHTML = svgCode;
                    pre.replaceWith(div);
                    setTimeout(function () { try { window.dispatchEvent(new Event('resize')); } catch (_) { } }, 50);
                } catch (e2) {
                    // If swap fails, leave code as-is
                }
            }, pre);
        } catch (e) {
            // If render throws, leave code as-is
        }
    }

    function render() {
        if (!window.mermaid) return; // keep original code blocks visible
        try {
            if (isRendering) return;
            isRendering = true;
            window.mermaid.initialize({ startOnLoad: false, securityLevel: 'loose', theme: 'neutral', flowchart: { curve: 'linear' } });
            document.querySelectorAll('pre code').forEach(renderOne);
            setTimeout(function () { isRendering = false; lastRender = Date.now(); }, 100);
        } catch (e) {
            console.warn('Mermaid render error', e);
            isRendering = false;
        }
    }

    function schedule() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', render);
        } else {
            render();
        }
        window.addEventListener('hashchange', function () { setTimeout(render, 80); });
        var content = document.querySelector('.md-content');
        if (content && 'MutationObserver' in window) {
            var obs = new MutationObserver(function (mutations) {
                if (isRendering) return;
                var now = Date.now();
                if (now - lastRender < 100) return;
                for (var i = 0; i < mutations.length; i++) {
                    if (mutations[i].addedNodes && mutations[i].addedNodes.length) { setTimeout(render, 50); break; }
                }
            });
            obs.observe(content, { childList: true, subtree: true });
        }
    }

    schedule();
})();
