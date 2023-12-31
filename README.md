# PaperMC Web API

[![Deploy build artifacts](https://github.com/Frank-Mayer/papermc-web-api/actions/workflows/deploy.yml/badge.svg)](https://github.com/Frank-Mayer/papermc-web-api/actions/workflows/deploy.yml)

[![this product is ai free](https://this-product-is-ai-free.github.io/badge.svg)](https://this-product-is-ai-free.github.io)

## Requirements

- [PaperMC](https://papermc.io/) server. Look at the `api-version` in [`paper-plugin.yml`](https://github.com/Frank-Mayer/papermc-web-api/blob/main/src/main/resources/paper-plugin.yml) to see the minimum version.

## Setup

1. Download the latest [jar file](https://frank-mayer.github.io/papermc-web-api/papermc-web-api.jar) and put it into your plugins folder.
1. Start the Paper server. This plugin will create a default config file for you.
1. Stop the Paper server.
1. Edit the config file. [Read the docs](https://github.com/Frank-Mayer/papermc-web-api/wiki).
1. You are good to go.

## Disclaimer

This plugin uses the [`com.sun.net.httpserver.HttpServer`](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) with the `http` protocol wich is not encrypted.
If your API is publicly avaliable you should use a proxy like [NGINX](https://www.nginx.com/) to encrypt its connection.

This server is resource intensive because it requires reflection.
I tried to do as much reflection as possible once at program startup to avoid it during runtime.

Read more about the reasoning [here](https://blogs.oracle.com/javamagazine/post/java-reflection-performance)

## Documentation

There is a [wiki](https://github.com/Frank-Mayer/papermc-web-api/wiki) for this repository containing the full documentation.
