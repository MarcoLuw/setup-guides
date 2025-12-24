from logging_config import logging_config

# Create loggers with just names and levels - handlers come from root
def get_logger(name: str, level: int = None):
    """Get a logger following best practices"""
    if level is None:
        # Set appropriate default levels for different components
        level_map = {
            "fastapi": logging.INFO,
            "orchestrator": logging.INFO, 
            "deployer": logging.INFO,
            "access": logging.INFO,
        }
        level = level_map.get(name, logging.INFO)
    
    return logging_config.get_logger(name, level)

# Pre-configured loggers for convenience
fastapi_logger = get_logger("fastapi")
orchestrator_logger = get_logger("orchestrator") 
deployer_logger = get_logger("deployer")
access_logger = get_logger("access")