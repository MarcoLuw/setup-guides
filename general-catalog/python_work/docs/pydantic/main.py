## config imports
from core.config import load_config


config = load_config()

RAILWAY_USERNAME = config.RAILWAY_USERNAME
RAILWAY_PASSWORD = config.RAILWAY_PASSWORD
clone_directory = config.github.clone_directory
api_endpoint = config.openrouter.api_endpoint
model = config.openrouter.model
google_gemini_model = config.google_gemini.model

print(f"Using Railway Username: {RAILWAY_USERNAME}")
print(f"Using Railway Password: {RAILWAY_PASSWORD}")
print(f"Clone Directory: {clone_directory}")
print(f"OpenRouter API Endpoint: {api_endpoint}")
print(f"OpenRouter Model: {model}")
print(f"Google Gemini Model: {google_gemini_model}")