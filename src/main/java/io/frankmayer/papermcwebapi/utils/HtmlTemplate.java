package io.frankmayer.papermcwebapi.utils;

import io.frankmayer.papermcwebapi.HttpFrontend;

public class HtmlTemplate {
    public static String li(final String className, final Iterable<String> values) {
        final StringBuilder sb = new StringBuilder();
        for (final String scope : values) {
            sb.append("<li class=\"").append(HttpFrontend.escapeHtml(className)).append("\">").append(scope).append("</li>");
        }
        return sb.toString();
    }

    public static String li(final String className, final String[] values) {
        final StringBuilder sb = new StringBuilder();
        for (final String scope : values) {
            sb.append("<li class=\"").append(HttpFrontend.escapeHtml(className)).append("\">").append(scope).append("</li>");
        }
        return sb.toString();
    }

    private final StringBuilder body = new StringBuilder();

    private final String mainClass;

    public HtmlTemplate(final String mainClass) {
        this.mainClass = mainClass;
    }

    public HtmlTemplate() {
        this.mainClass = "";
    }

    public HtmlTemplate append(final String value) {
        this.body.append(value);
        return this;
    }

    public String process(final Object... args) {
        return Str.html("<main class=\"" + HttpFrontend.escapeHtml(this.mainClass) + "\">" +
                String.format(this.body.toString(), args)
                + "</main>");
    }

    @Override
    public String toString() {
        return this.body.toString();
    }

    public HtmlTemplate p(final String className, final Object content) {
        return this.append("<p class=\"").append(HttpFrontend.escapeHtml(className)).append("\">")
                .append(content.toString())
                .append("</p>");
    }

    public HtmlTemplate ul(final String className, final Object content) {
        return this.append("<ul class=\"").append(HttpFrontend.escapeHtml(className)).append("\">")
                .append(content.toString())
                .append("</ul>");
    }

    public HtmlTemplate form(
            final String className,
            final String method,
            final String action,
            final boolean autocomplete,
            final Object content) {
        return this.append(
                String.format(
                        "<form class=\"%s\" method=\"%s\" action=\"%s\"%s>",
                        HttpFrontend.escapeHtml(className),
                        method,
                        action,
                        autocomplete ? "" : " autocomplete=\"off\""))
                .append(content.toString())
                .append("</form>");
    }

    public HtmlTemplate input(
            final String className,
            final String type,
            final String name,
            final boolean required,
            final String value) {
        return this.append(
                String.format(
                        "<input class=\"%s\" type=\"%s\" name=\"%s\" value=\"%s\"%s/>",
                        className == null ? "" : HttpFrontend.escapeHtml(className),
                        type == null ? "text" : type,
                        name == null ? "" : name,
                        value == null ? "" : value,
                        required ? " required" : ""));
    }
}
