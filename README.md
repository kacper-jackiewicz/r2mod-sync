# R2ModSync

R2ModSync is a web application for managing a game server's mod profile with underlying [r2modman-headless](https://github.com/mpawlowski/r2modman-headless):

Sign in to check server status, upload an `.r2z` profile or restore the vanilla setup, and download the profile currently installed on the server.
The application coordinates server operations, applies profile updates, and reports their progress in the web interface. 
It is designed to be run as a separate container and check server status by docker-client. Tests with image [mbround18/valheim](https://hub.docker.com/r/mbround18/valheim)
Sample docker-compose.yml provided.

The project is a Maven reactor with separate `frontend` (React/TypeScript assets) and `backend` (Spring Boot application) modules. The frontend module builds its static assets into a JAR consumed by the backend, and the Docker image runs the packaged application as a single service. Build the full project with `mvn package` or build the image with `docker build -t r2mod-sync .`.

This is a small project intended to let non-technical friends update mods on their dedicated server.