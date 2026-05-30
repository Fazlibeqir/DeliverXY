import type { WebView } from "@nativescript/core";

export function runWebViewJavaScript(webView: WebView, script: string): void {
  if (!webView) return;
  if (webView.ios) {
    webView.ios.evaluateJavaScriptCompletionHandler(script, null);
  } else if (webView.android) {
    webView.android.loadUrl(`javascript:${script}`);
  }
}

export function installWebViewBridge(
  webView: WebView,
  onMessage: (msg: { type: string; data?: unknown }) => void
): void {
  webView.on("loadStarted", (args: { url?: string; cancel?: boolean }) => {
    const url = args.url || "";
    if (!url.startsWith("nsbridge://")) return;
    args.cancel = true;
    try {
      const json = decodeURIComponent(url.slice("nsbridge://".length));
      onMessage(JSON.parse(json));
    } catch {
      // ignore malformed bridge payloads
    }
  });
}

export function injectNsEmitBridge(webView: WebView): void {
  runWebViewJavaScript(
    webView,
    `window.nsEmit = function(msg) {
      try {
        window.location.href = 'nsbridge://' + encodeURIComponent(JSON.stringify(msg));
      } catch (e) {}
    }; true;`
  );
}
