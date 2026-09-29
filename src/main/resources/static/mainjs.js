/* JWT demo client (vanilla JS, no CDN). Used by login.html and profile.html.
 * The token is kept in localStorage as in the course document; note this is readable by any
 * XSS on this origin. All server data is written with textContent, never innerHTML. */
(function () {
  'use strict';
  var TOKEN_KEY = 'token';

  function $(id) { return document.getElementById(id); }
  function say(text, kind) {
    var el = $('feedback');
    if (!el) { return; }
    el.textContent = text || '';
    el.className = 'msg' + (kind ? ' ' + kind : '');
  }
  function getToken() { return localStorage.getItem(TOKEN_KEY); }
  function clearToken() { localStorage.removeItem(TOKEN_KEY); }

  function api(method, url, body, useToken) {
    var headers = { 'Accept': 'application/json' };
    if (body !== undefined) { headers['Content-Type'] = 'application/json'; }
    if (useToken && getToken()) { headers['Authorization'] = 'Bearer ' + getToken(); }
    return fetch(url, {
      method: method,
      headers: headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    }).then(function (res) {
      return res.text().then(function (text) {
        var data = null;
        try { data = text ? JSON.parse(text) : null; } catch (e) { data = null; }
        if (!res.ok) {
          var err = new Error((data && data.message) || ('HTTP ' + res.status));
          err.status = res.status;
          throw err;
        }
        return data;
      });
    });
  }

  /* ---------------- login page ---------------- */
  function initLogin() {
    var loginForm = $('login-form');
    var signupForm = $('signup-form');
    if (getToken()) { window.location.href = '/profile'; return; }

    function show(which) {
      var isLogin = which === 'login';
      loginForm.classList.toggle('hidden', !isLogin);
      signupForm.classList.toggle('hidden', isLogin);
      $('tab-login').classList.toggle('active', isLogin);
      $('tab-signup').classList.toggle('active', !isLogin);
      say('');
    }
    $('tab-login').addEventListener('click', function () { show('login'); });
    $('tab-signup').addEventListener('click', function () { show('signup'); });

    loginForm.addEventListener('submit', function (e) {
      e.preventDefault();
      var btn = $('Login'); btn.disabled = true; say('');
      api('POST', '/auth/login', { email: $('email').value, password: $('password').value })
        .then(function (data) {
          localStorage.setItem(TOKEN_KEY, data.token);
          window.location.href = '/profile';
        })
        .catch(function (err) { say(err.status === 401 ? 'Login failed: wrong email or password.' : err.message, 'error'); })
        .then(function () { btn.disabled = false; });
    });

    signupForm.addEventListener('submit', function (e) {
      e.preventDefault();
      var body = {
        fullName: $('su-name').value,
        email: $('su-email').value,
        password: $('su-password').value
      };
      var img = $('su-images').value.trim();
      if (img) { body.images = img; }
      api('POST', '/auth/signup', body)
        .then(function () { show('login'); $('email').value = body.email; say('Account created. Please log in.', 'ok'); })
        .catch(function (err) { say(err.message, 'error'); });
    });
  }

  /* ---------------- profile page ---------------- */
  function safeImageUrl(u) {
    if (!u) { return null; }
    return /^(https?:\/\/|\/)/i.test(u) && u.indexOf('//') !== 0 ? u : null;
  }

  function initProfile() {
    if (!getToken()) { window.location.href = '/login'; return; }

    $('logout').addEventListener('click', function () {
      clearToken();                       // stateless JWT: logout = drop the token client-side
      window.location.href = '/login';
    });

    api('GET', '/users/me', undefined, true)
      .then(function (u) {
        $('profile').textContent = u.fullName;
        $('p-email').textContent = u.email;
        $('p-role').textContent = u.role;
        var url = safeImageUrl(u.images);
        if (url) { var img = $('images'); img.src = url; img.classList.remove('hidden'); }
      })
      .catch(function (err) {
        if (err.status === 401 || err.status === 403) {   // expired / invalid token
          clearToken();
          window.location.href = '/login';
        } else {
          say(err.message, 'error');
        }
      });
  }

  document.addEventListener('DOMContentLoaded', function () {
    if ($('login-form')) { initLogin(); } else if ($('profile')) { initProfile(); }
  });
})();
