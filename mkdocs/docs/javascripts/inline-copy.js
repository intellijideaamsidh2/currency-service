(function () {
    function getContentRoot() {
        return document.querySelector('.md-content') || document;
    }

    function addCopyButtons(root) {
        root = root || getContentRoot();
        // Target any inline code within content, skip fenced blocks
        var codes = root.querySelectorAll('code:not(.no-copy)');
        codes.forEach(function (code) {
            if (code.closest('pre')) return; // skip fenced blocks (already have copy)
            if (code.dataset.copyInjected === '1') return;
            code.dataset.copyInjected = '1';

            var wrapper = document.createElement('span');
            wrapper.className = 'inline-copy-wrap';
            code.parentNode.insertBefore(wrapper, code);
            wrapper.appendChild(code);

            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'inline-copy-btn';
            btn.title = 'Copy';
            btn.setAttribute('aria-label', 'Copy');
            btn.innerHTML = '⎘';
            wrapper.appendChild(btn);

            btn.addEventListener('click', function () {
                var text = code.innerText || code.textContent || '';
                navigator.clipboard.writeText(text).then(function () {
                    btn.classList.add('copied');
                    btn.title = 'Copied!';
                    setTimeout(function () { btn.classList.remove('copied'); btn.title = 'Copy'; }, 1200);
                }).catch(function () {
                    try {
                        var r = document.createRange();
                        r.selectNodeContents(code);
                        var sel = window.getSelection();
                        sel.removeAllRanges();
                        sel.addRange(r);
                        document.execCommand('copy');
                        sel.removeAllRanges();
                    } catch (e) { }
                });
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () { addCopyButtons(getContentRoot()); });
    } else { addCopyButtons(getContentRoot()); }

    // Re-run on navigation within Material theme
    var content = getContentRoot();
    if (content && 'MutationObserver' in window) {
        var obs = new MutationObserver(function (mutations) {
            for (var i = 0; i < mutations.length; i++) {
                if (mutations[i].addedNodes && mutations[i].addedNodes.length) {
                    addCopyButtons(content);
                    break;
                }
            }
        });
        obs.observe(content, { childList: true, subtree: true });
    }
})();
