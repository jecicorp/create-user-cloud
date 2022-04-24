<#macro head>
<head>
  <meta charset="utf-8" />
  <title>Pristy - Gestion Documentaire en ligne</title>
  <!-- mobile responsive meta -->
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=5" />
  <meta name="description" content="Page d'inscription à Pristy Cloud" />
  <meta name="author" content="Jeci SARL" />
  <meta property="og:title" content="Pristy - Gestion Documentaire en ligne" />
  <meta property="og:description" content="Page d'inscription à Pristy Cloud" />
  <meta property="og:type" content="website" />
  <meta property="og:url" content="https://cloud.pristy.net/alfresco/s/fr/jeci/signin/create-user" />
  <meta property="og:image" content="https://pristy.fr/images/logo.png" />
  <meta property="og:site_name" content="Pristy - Gestion Documentaire" />

  <link rel="shortcut icon" href="https://pristy.fr/images/favicon.png" type="image/x-icon" />
  <link rel="icon" href="https://pristy.fr/images/favicon.png" type="image/x-icon" />

  <style>
    *,
    ::after,
    ::before {
      box-sizing: border-box
    }

    html {
      font-family: sans-serif;
      line-height: 1.15;
      -webkit-text-size-adjust: 100%;
      -webkit-tap-highlight-color: transparent
    }

    article,
    aside,
    figcaption,
    figure,
    footer,
    header,
    hgroup,
    main,
    nav,
    section {
      display: block
    }

    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, segoe ui, Roboto, helvetica neue, Arial, noto sans, sans-serif, apple color emoji, segoe ui emoji, segoe ui symbol, noto color emoji;
      font-size: 1rem;
      font-weight: 400;
      line-height: 1.5;
      color: #212529;
      text-align: left;
      background-color: #fff;
      overflow-x: hidden
    }

    h1, h2, h3, h4, h5, h6 {
      margin-top: 0;
      margin-bottom: .5rem;
      font-weight: 500;
      line-height: 1.2
    }

    .h1, h1 {
      font-size: 2.5rem
    }

    .h2, h2 {
      font-size: 2rem
    }

    .h3, h3 {
      font-size: 1.75rem
    }

    .h4, h4 {
      font-size: 1.5rem
    }

    .h5, h5 {
      font-size: 1.25rem
    }

    .h6, h6 {
      font-size: 1rem
    }

    .container {
      width: 100%;
      padding-right: 15px;
      padding-left: 15px;
      margin-right: auto;
      margin-left: auto;
    }

    @media(min-width:576px) {
      .container {
        max-width: 540px
      }
    }

    @media(min-width:768px) {
      .container {
        max-width: 720px
      }
    }

    @media(min-width:992px) {
      .container {
        max-width: 960px
      }
    }

    @media(min-width:1200px) {
      .container {
        max-width: 1140px
      }
    }

    .container-fluid, .container-lg, .container-md, .container-sm, .container-xl {
      width: 100%;
      padding-right: 15px;
      padding-left: 15px;
      margin-right: auto;
      margin-left: auto
    }

    @media(min-width:576px) {
      .container, .container-sm {
        max-width: 540px
      }
    }

    @media(min-width:768px) {
      .container, .container-md, .container-sm {
        max-width: 720px
      }
    }

    @media(min-width:992px) {
      .container, .container-lg, .container-md, .container-sm {
        max-width: 960px
      }
    }

    @media(min-width:1200px) {
      .container, .container-lg, .container-md, .container-sm, .container-xl {
        max-width: 1140px
      }
    }

    .row {
      display: -ms-flexbox;
      display: flex;
      -ms-flex-wrap: wrap;
      flex-wrap: wrap;
    }

    p {
      margin-top: 0;
      margin-bottom: 1rem
    }

    img {
      vertical-align: middle;
      border-style: none
    }

    a, a:hover, a:focus {
      text-decoration: none
    }

    a, button, select {
      cursor: pointer;
      transition: .2s ease
    }

    a:focus, button:focus, select:focus {
      outline: 0
    }

    a:hover {
      color: #fff
    }

    button {
      border-radius: 0
    }

    button:focus {
      outline: 1px dotted;
      outline: 5px auto -webkit-focus-ring-color
    }

    button, input, span, optgroup, select, textarea {
      margin: 0;
      font-family: inherit;
      font-size: inherit;
      line-height: inherit
    }

    button, input, span {
      overflow: visible
    }

    button, select {
      text-transform: none
    }

    select {
      word-wrap: normal
    }

    .section {
      padding-top: 70px;
      padding-bottom: 70px
    }

    .section-title {
      margin-bottom: 30px;
      color: #777
    }

    .bg-cover {
      background-size: cover;
      background-position: 50%;
      background-repeat: no-repeat
    }

    .navigation {
      background-color: #fda18b
    }

    .banner {
      background-image: url('https://pristy.fr/images/bubble-banner.png');
      background-repeat: no-repeat;
      padding: 30px 0;
      height: 100%;
    }

    .card {
      position: relative;
      display: -ms-flexbox;
      display: flex;
      -ms-flex-direction: column;
      flex-direction: column;
      min-width: 0;
      word-wrap: break-word;
      background-color: #fff;
      background-clip: border-box;
      border: 1px solid rgba(0, 0, 0, .125);
      border-radius: .25rem
    }

    .navbar-brand {
      display: inline-block;
      padding-top: .3125rem;
      padding-bottom: .3125rem;
      margin-right: 1rem;
      font-size: 1.25rem;
      line-height: inherit;
      white-space: nowrap;
    }

    .btn {
      font-size: 16px;
      font-family: lato, sans-serif;
      font-weight: 600;
      text-transform: capitalize;
      padding: 15px 30px;
      border-radius: 30px;
      border: 0;
      border-top-color: currentcolor;
      border-right-color: currentcolor;
      border-bottom-color: currentcolor;
      border-left-color: currentcolor;
      position: relative;
      z-index: 1;
      transition: .2s ease;
      white-space: nowrap;
    }

    .btn-primary {
      background-color: #ffd44f;
      color: #222;
      border-color: #ffd44f;
    }

    .btn::before {
      position: absolute;
      content: "";
      height: 100%;
      width: 100%;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: -1;
      border-radius: inherit;
      transition: inherit;
      background-color: inherit;
      border: 1px solid transparent;
    }

    label, span {
      min-width: 10em;
      display: inline-block;
    }
  </style>
</head>
</#macro>