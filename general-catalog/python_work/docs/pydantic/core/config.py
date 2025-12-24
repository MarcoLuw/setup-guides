import yaml
from pathlib import Path
from typing import List, Optional, Dict
from functools import lru_cache
from pydantic import BaseModel
from pydantic_settings import BaseSettings, SettingsConfigDict

# Define nested models
class GitHubSettings(BaseModel):
    clone_directory: str

class OpenRouterSettings(BaseModel):
    api_endpoint: str
    model: str 

class GoogleGeminiSettings(BaseModel):
    model: str

class AppSettings(BaseSettings):
    github: GitHubSettings
    openrouter: OpenRouterSettings
    google_gemini: GoogleGeminiSettings

    ## .env vars
    RAILWAY_USERNAME: str
    RAILWAY_PASSWORD: str

    @classmethod
    def get_from_yaml_env(cls, yaml_path: Path):
        """
        Load settings from a YAML file.
        Args:
            yaml_path (Path): Path to the YAML file.
        Returns:
            AppSettings: An instance of AppSettings populated with the data from the YAML file.
        """
        yaml_data = {}
        if not yaml_path.exists():
            raise FileNotFoundError(f"YAML file not found: {yaml_path}")
        with open(yaml_path, 'r') as file:
            yaml_data = yaml.safe_load(file) or {}
        
        # Pass YAML values as keyword args to BaseSettings
        return cls(**yaml_data)

    model_config = SettingsConfigDict(case_sensitive=True, env_file='.env', env_file_encoding='utf-8')

@lru_cache()
def load_config() -> AppSettings:
    """
    Load the application settings from a YAML file and environment variables.
    Returns:
        AppSettings: An instance of AppSettings populated with the data from the YAML file and environment variables.
    """
    yaml_path = Path(__file__).resolve().parent.parent / "config" / "settings.yaml"
    print(f"Loading configuration from: {yaml_path}")
    settings = AppSettings.get_from_yaml_env(yaml_path)
    
    return settings