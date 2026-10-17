(() => {
    const storageKey = "r2mod-sync-theme";
    const toggle = document.querySelector(".theme-toggle");
    const label = document.querySelector("#theme-toggle-label");
    const icon = toggle?.querySelector(".theme-toggle-icon");
    const translate = (key) => window.r2modSyncTranslate?.(key) ?? key;

    const applyTheme = (theme) => {
        document.documentElement.dataset.theme = theme;
        const darkMode = theme === "dark";
        const labelKey = darkMode ? "theme.lightMode" : "theme.darkMode";
        const ariaLabelKey = darkMode ? "theme.switchToLight" : "theme.switchToDark";
        if (label) {
            label.textContent = translate(labelKey);
        }
        if (toggle) {
            toggle.setAttribute("aria-label", translate(ariaLabelKey));
        }
        if (icon) {
            icon.textContent = darkMode ? "☀" : "☾";
        }
    };

    const savedTheme = localStorage.getItem(storageKey);
    applyTheme(savedTheme === "dark" ? "dark" : "light");

    toggle?.addEventListener("click", () => {
        const nextTheme = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
        localStorage.setItem(storageKey, nextTheme);
        applyTheme(nextTheme);
    });

    document.addEventListener("r2mod-sync-translations-ready", () => {
        applyTheme(document.documentElement.dataset.theme === "dark" ? "dark" : "light");
    }, {once: true});
})();
