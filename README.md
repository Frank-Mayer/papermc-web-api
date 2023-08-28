# PaperMC Web API

[![Deploy build artifacts](https://github.com/Frank-Mayer/papermc-web-api/actions/workflows/deploy.yml/badge.svg)](https://github.com/Frank-Mayer/papermc-web-api/actions/workflows/deploy.yml)

[![this product is ai free](https://this-product-is-ai-free.github.io/badge.svg)](https://this-product-is-ai-free.github.io)

## Disclaimer

This plugin uses the [`com.sun.net.httpserver.HttpServer`](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) with the `http` protocol wich is not encrypted.
If your API is publicly avaliable you should use a proxy like [NGINX](https://www.nginx.com/) to encrypt its connection.
