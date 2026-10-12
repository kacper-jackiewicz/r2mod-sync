export function showMessage(messageKey: string, operationId?: string) {
    const target = new URL("/", window.location.href);
    target.searchParams.set("page", "server-message");
    if (operationId) {
        target.searchParams.set("operationId", operationId);
    } else {
        target.searchParams.set("messageKey", messageKey);
    }
    window.location.assign(target);
}

export async function showResponseMessage(response: Response): Promise<boolean> {
    const result = await response.clone().json().catch(() => null) as
        {operationId?: unknown; messageKey?: unknown} | null;
    if (typeof result?.operationId === "string") {
        showMessage("", result.operationId);
        return true;
    }
    if (typeof result?.messageKey === "string") {
        showMessage(result.messageKey);
        return true;
    }
    return false;
}
