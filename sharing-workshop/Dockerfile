# Use an Nginx base image for serving static files
FROM nginx:alpine

# Set the working directory
WORKDIR /usr/share/nginx/html

# Copy your RemarkJS slides and static files to the Nginx HTML directory
COPY ./docs/slides /usr/share/nginx/html

# Expose the default Nginx port
EXPOSE 80

# Start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]