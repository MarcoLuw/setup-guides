import logging
import logging.handlers
import os
from pathlib import Path
from typing import Dict, Any

class LoggingConfig:
    """Centralized logging configuration following best practices"""
    
    def __init__(self, log_dir: str = "logs"):
        self.log_dir = Path(log_dir)
        self.log_dir.mkdir(exist_ok=True)
        self._root_configured = False
    
    def setup_root_logging(self):
        """Configure the root logger with all handlers - do this once"""
        if self._root_configured:
            return
            
        root_logger = logging.getLogger()
        root_logger.setLevel(logging.DEBUG)  # Set to lowest level needed
        
        # Clear any existing handlers
        root_logger.handlers.clear()
        
        # Standard format for most logs
        standard_format = "%(asctime)s - %(name)s - %(levelname)s - [%(filename)s:%(lineno)d] - %(message)s"
        access_format = "%(asctime)s - %(message)s"
        
        # Console handler - shows INFO and above
        console_handler = logging.StreamHandler()
        console_handler.setLevel(logging.INFO)
        console_handler.setFormatter(logging.Formatter(standard_format))
        
        # Add filter to exclude access logs from console
        console_handler.addFilter(lambda record: not record.name.startswith('access'))
        root_logger.addHandler(console_handler)
        
        # Application log file - shows DEBUG and above
        app_file_handler = logging.handlers.RotatingFileHandler(
            self.log_dir / "application.log",
            maxBytes=10*1024*1024,  # 10MB
            backupCount=5
        )
        app_file_handler.setLevel(logging.DEBUG)
        app_file_handler.setFormatter(logging.Formatter(standard_format))
        
        # Add filter to exclude access logs from main app log
        app_file_handler.addFilter(lambda record: not record.name.startswith('access'))
        root_logger.addHandler(app_file_handler)
        
        # Separate access log file
        access_file_handler = logging.handlers.RotatingFileHandler(
            self.log_dir / "access.log",
            maxBytes=10*1024*1024,  # 10MB
            backupCount=5
        )
        access_file_handler.setLevel(logging.INFO)
        access_file_handler.setFormatter(logging.Formatter(access_format))
        
        # Only handle access logs
        access_file_handler.addFilter(lambda record: record.name.startswith('access'))
        root_logger.addHandler(access_file_handler)
        
        # Error log file - only ERROR and CRITICAL
        error_file_handler = logging.handlers.RotatingFileHandler(
            self.log_dir / "errors.log",
            maxBytes=10*1024*1024,  # 10MB
            backupCount=10  # Keep more error logs
        )
        error_file_handler.setLevel(logging.ERROR)
        error_file_handler.setFormatter(logging.Formatter(standard_format))
        root_logger.addHandler(error_file_handler)
        
        self._root_configured = True
    
    def get_logger(self, name: str, level: int = logging.INFO) -> logging.Logger:
        """Get a logger with specified name and level - handlers come from root"""
        
        # Ensure root logging is configured
        self.setup_root_logging()
        
        # Get the logger (will inherit handlers from root via propagation)
        logger = logging.getLogger(name)
        logger.setLevel(level)  # Only set the level, handlers come from root
        
        # Keep propagation enabled (default True) so messages go to root handlers
        # logger.propagate = True  # This is the default, no need to set explicitly
        
        return logger
    
    def suppress_noisy_loggers(self):
        """Suppress logs from noisy third-party libraries"""
        noisy_loggers = [
            "httpx",
            "google_genai.models", 
            "urllib3.connectionpool",
            "asyncio",
            "multipart.multipart",
            "uvicorn.access",  # Uvicorn access logs handled separately
        ]
        
        for logger_name in noisy_loggers:
            logging.getLogger(logger_name).setLevel(logging.WARNING)

# Global logging config instance
logging_config = LoggingConfig()

# Setup root logging immediately
logging_config.setup_root_logging()
logging_config.suppress_noisy_loggers()