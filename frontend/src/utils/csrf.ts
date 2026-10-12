export function readCsrfToken(): string {
    const cookie = document.cookie.split("; ").find((value) => value.startsWith("XSRF-TOKEN="));
    return cookie ? decodeURIComponent(cookie.slice("XSRF-TOKEN=".length)) : "";
}
