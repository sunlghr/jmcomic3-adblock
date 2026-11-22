package com.jmcomic3.adblock

/**
 * The page is a Capacitor/React bundle. HttpUtil.tryDecryption decrypts ad
 * responses with AES-256-ECB. For the two modern routes (ad_content_all and
 * advertise_all) it uses a fixed key, so their empty
 * payloads can be pre-encrypted here without touching the app's JS bundle.
 *
 * The legacy advertise/ad_content routes use md5(time + content), a key that is
 * generated at page load. They cannot be pre-encrypted; returning an empty data
 * string makes tryDecryption fall back to "", which the app treats as no ads.
 */
object AdBlockScript {

    const val MARKER = "__JMCOMIC3_ADBLOCK_V2__"

    private const val ALL_CIPHER = "dX0+bVp3SRiz9JgJKje5Gw=="
    private const val COVER_CIPHER =
        "E85GEiYAWbIxlTaAd9FS/hV1ZIgh3/7CLDz+K7/urrTltQw4CU16UCPM3pumB2vmCsnyuBk2q9yX3/NDU9zLkYkEt3DJ7fvNNJM221fpSn8MJCOZcFlQkRmMCQ1VhvbzsHZJyG+9xKHEpbEXq4xXX3nfoRrHluFEQ4uUo62rY0bV8D1qUhReMG5bgVFbn4g5vS8ogxLJ2F0+XyvNbny9tQ6g0bX3WhwPJVVuAUmCfB0="
    private const val SAFE_COVER_JSON =
        """{"stype":{"app_detail_between_author_and_related":[]},"link":{"exchange_link":{"show_max":1,"first_links":[],"second_links":[]}},"img":{"splash_top":{"advs":[]},"pop1_list":{"advs":[]}}}"""

    fun build(config: BlockConfig): String = """
(function () {
  'use strict';
  var MARKER = '$MARKER';
  if (window[MARKER]) return;
  window[MARKER] = true;
  try { console.log('[JMAdBlock] document-start active: ' + location.href); } catch (e) {}
  var CFG = { home: ${config.blockHome}, pages: ${config.blockPages}, media: ${config.blockMedia} };
  var ALL = '$ALL_CIPHER';
  var COVER = '$COVER_CIPHER';
  var SAFE_COVER = $SAFE_COVER_JSON;
  var FAKE_KEY = '__JM_ADBLOCK_FAKE__';

  try {
    if (localStorage.getItem(FAKE_KEY) === '1') {
      if (!CFG.home) localStorage.removeItem('adsList');
      if (!CFG.pages) {
        localStorage.removeItem('adsContent');
        localStorage.removeItem('oldAdsCache');
      }
    }
    if (CFG.home) localStorage.setItem('adsList', '{}');
    if (CFG.pages) localStorage.setItem('adsContent', JSON.stringify(SAFE_COVER));
    localStorage.setItem(FAKE_KEY, (CFG.home || CFG.pages || CFG.media) ? '1' : '0');
    localStorage.setItem('__JMADBLOCK_SCRIPT_VERSION__', '1.1.3');
    // The app's launch flow renders an ad-close splash page (step 3) even when
    // every ad slot is empty. `state=true` makes Main resume directly at the
    // home step, skipping the entire splash/cover ad sequence.
    try {
      if (CFG.home || CFG.pages) {
        sessionStorage.setItem('state', 'true');
        localStorage.setItem('__JMADBLOCK_SKIP_SPLASH__', '1');
      }
    } catch (e) {}
  } catch (e) {}

  // Home renders the app_home_top slider at a fixed 250dp height even after
  // its ad payload is removed. Hide only that specific slider; other Swipers
  // (reader, related works, novels) keep their normal layout.
  function installHomeBannerCss() {
    try {
      if (!CFG.home) return;
      if (document.getElementById('__JMADBLOCK_CSS__')) return;
      var style = document.createElement('style');
      style.id = '__JMADBLOCK_CSS__';
      style.textContent = '[class~="mySwiper"][class~="h-[250px]"]{display:none!important}';
      (document.head || document.documentElement).appendChild(style);
    } catch (e) {}
  }
  installHomeBannerCss();
  try {
    document.addEventListener('DOMContentLoaded', installHomeBannerCss);
  } catch (e) {}

  var originalFetch = window.fetch;
  var currentFetch = typeof originalFetch === 'function' ? originalFetch : null;
  var dispatching = false;
  var wrapper = function (input, init) {
    var url = '';
    try {
      if (typeof input === 'string') url = input;
      else if (input && typeof input.url === 'string') url = input.url;
      else url = String(input);
    } catch (e) {}
    var clean = url.split('#')[0].split('?')[0].replace(/\/+$/, '');
    var name = clean.substring(clean.lastIndexOf('/') + 1).toLowerCase();
    var payload = null;
    if (name === 'ad_content_all' && CFG.home) payload = ALL;
    else if (name === 'advertise_all' && CFG.pages) payload = COVER;
    else if ((name === 'advertise' || name === 'ad_content') && CFG.media) payload = '';
    if (payload !== null) {
      try {
        window.__JMADBLOCK_HITS__ = window.__JMADBLOCK_HITS__ || [];
        window.__JMADBLOCK_HITS__.push(name);
        localStorage.setItem('__JMADBLOCK_LAST_HIT__', name);
        console.log('[JMAdBlock] blocked endpoint: ' + name);
      } catch (e) {}
      var body = JSON.stringify({ code: 200, data: payload });
      return Promise.resolve(new Response(body, {
        status: 200,
        headers: { 'Content-Type': 'application/json' }
      }));
    }
    if (dispatching) {
      return originalFetch.call(this, input, init);
    }
    var fn = currentFetch || originalFetch;
    if (typeof fn !== 'function') {
      return Promise.reject(new TypeError('fetch is unavailable'));
    }
    dispatching = true;
    try {
      // Keep the re-entry guard set until the transport promise settles.
      // Capacitor calls CapacitorWebFetch asynchronously; clearing the flag
      // immediately after fn() returns can recurse back into native HTTP.
      var result = fn.apply(this, arguments);
      return Promise.resolve(result).finally(function () { dispatching = false; });
    } catch (e) {
      dispatching = false;
      throw e;
    }
  };

  // Capacitor's native-bridge replaces window.fetch after document-start.
  // Keep a stable accessor so the ad wrapper cannot be overwritten; later
  // assignments only replace the underlying transport.
  function installFetchGuard() {
    try {
      Object.defineProperty(window, 'fetch', {
        configurable: true,
        enumerable: true,
        get: function () { return wrapper; },
        set: function (value) {
          if (typeof value === 'function') currentFetch = value;
        }
      });
      localStorage.setItem('__JMADBLOCK_FETCH_GUARD__', 'defineProperty');
    } catch (e) {
      try {
        window.fetch = wrapper;
        localStorage.setItem('__JMADBLOCK_FETCH_GUARD__', 'assign');
      } catch (e2) {}
    }
  }

  if (typeof originalFetch === 'function') {
    installFetchGuard();
    try {
      setInterval(function () {
        try {
          if (window.fetch !== wrapper) installFetchGuard();
        } catch (e) {}
      }, 1000);
    } catch (e) {}
  }
  window.__JMADBLOCK_ACTIVE__ = true;
  try {
    document.addEventListener('DOMContentLoaded', function () {
      document.documentElement.setAttribute('data-jm-adblock', 'active');
    });
  } catch (e) {}
})();
""".trimIndent()

    /** Restores the real endpoints when the master switch is turned off. */
    fun cleanup(): String = """
(function () {
  try {
    if (localStorage.getItem('__JM_ADBLOCK_FAKE__') === '1') {
      localStorage.removeItem('adsList');
      localStorage.removeItem('adsContent');
      localStorage.removeItem('oldAdsCache');
      localStorage.removeItem('__JM_ADBLOCK_FAKE__');
    }
    localStorage.removeItem('__JMADBLOCK_SKIP_SPLASH__');
    try { sessionStorage.removeItem('state'); } catch (e) {}
    try {
      var css = document.getElementById('__JMADBLOCK_CSS__');
      if (css) css.remove();
    } catch (e) {}
    console.log('[JMAdBlock] disabled, real ad endpoints restored');
  } catch (e) {}
})();
""".trimIndent()
}
