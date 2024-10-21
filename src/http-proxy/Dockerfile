FROM caddy:2.8.4-alpine
EXPOSE 80
EXPOSE 443

# Copy in the custom Caddyfile
ARG CHAT_DOMAIN=localhost
ARG CHAT_MODULE_ADDRESS=chat:8080
RUN echo -e "${CHAT_DOMAIN} {\n    reverse_proxy ${CHAT_MODULE_ADDRESS}\n}" > /etc/caddy/Caddyfile
RUN test -e /etc/caddy/Caddyfile || (echo "Caddyfile was not copied over successfully" && false)
