FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build -- --configuration production


FROM nginx:alpine

COPY --from=build /app/dist /tmp/dist

RUN find /tmp/dist -name "index.html" -exec dirname {} \; | head -n 1 > /tmp/dist-path.txt \
    && cp -r $(cat /tmp/dist-path.txt)/* /usr/share/nginx/html/

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
# This is where Docker grabs the Nginx configuration file you just created in VS Code and overwrites Nginx's default settings with your custom routing rules!

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]