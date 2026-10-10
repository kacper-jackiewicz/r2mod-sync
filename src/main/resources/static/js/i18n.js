(() => {
    const scriptUrl = document.currentScript?.src;
    if (!scriptUrl) {
        console.error("Could not determine the translation file location.");
        return;
    }

    const translationsUrl = new URL("../i18n/translations.json", scriptUrl);
    const languageSelect = document.querySelector(".language-select");
    const supportedLanguages = ["en", "pl"];
    const savedLanguage = localStorage.getItem("r2mod-sync-language");
    let translations;

    const applyLanguage = (language) => {
        const dictionary = translations[language];
        document.documentElement.lang = language;
        document.querySelectorAll("[data-i18n]").forEach((element) => {
            const translation = dictionary[element.dataset.i18n];
            if (translation !== undefined) {
                element.textContent = translation;
            }
        });
        document.querySelectorAll("[data-i18n-placeholder]").forEach((element) => {
            const translation = dictionary[element.dataset.i18nPlaceholder];
            if (translation !== undefined) {
                element.setAttribute("placeholder", translation);
            }
        });
        document.querySelectorAll("[data-i18n-aria-label]").forEach((element) => {
            const translation = dictionary[element.dataset.i18nAriaLabel];
            if (translation !== undefined) {
                element.setAttribute("aria-label", translation);
            }
        });
        const title = document.querySelector("[data-i18n-title]");
        if (title) {
            const translation = dictionary[title.dataset.i18nTitle];
            if (translation !== undefined) {
                document.title = translation;
            }
        }
        if (languageSelect) {
            languageSelect.value = language;
        }
    };

    fetch(translationsUrl)
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Could not load translations: ${response.status}`);
            }
            return response.json();
        })
        .then((loadedTranslations) => {
            translations = loadedTranslations;
            const initialLanguage = supportedLanguages.includes(savedLanguage) ? savedLanguage : "en";
            applyLanguage(initialLanguage);
            window.r2modSyncTranslate = (key) => translations[document.documentElement.lang]?.[key] ?? key;
            document.dispatchEvent(new Event("r2mod-sync-translations-ready"));
            languageSelect?.addEventListener("change", () => {
                const selectedLanguage = languageSelect.value;
                if (supportedLanguages.includes(selectedLanguage)) {
                    localStorage.setItem("r2mod-sync-language", selectedLanguage);
                    window.location.reload();
                }
            });
        })
        .catch((error) => {
            console.error(error);
        });
})();
