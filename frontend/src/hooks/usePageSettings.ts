import {useEffect} from "react";
import type {Translate, TranslationKey} from "../types";

export function usePageSettings(
    language: string,
    titleKey: TranslationKey,
    pageClass: string,
    t: Translate
) {
    useEffect(() => {
        document.title = t(titleKey);
        document.body.className = pageClass;
    }, [language, pageClass, t, titleKey]);
}
