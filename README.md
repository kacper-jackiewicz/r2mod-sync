# R2ModSync

R2ModSync is a web application for managing a game server's mod profile with underlying [r2modman-headless](https://github.com/mpawlowski/r2modman-headless):

Sign in to check server status, upload an `.r2z` profile or restore the vanilla setup, and download the profile currently installed on the server.
The application coordinates server operations, applies profile updates, and reports their progress in the web interface. 
It is designed to be run as a separate container and check server status by docker-client. Tests with image [mbround18/valheim](https://hub.docker.com/r/mbround18/valheim)
Sample docker-compose.yml provided.

This application is not production ready. Web UI is completely copilot driven this should be properly change to the one of the Frontend framework and extracted out to separate module.
This is just small evening project to allow non-technical friends to update mods on their dedicated server.