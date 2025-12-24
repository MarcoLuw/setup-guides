import sys
import os
os.sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from logging import deployer_logger, orchestrator_logger

logger1 = deployer_logger
logger2 = orchestrator_logger

def log_message(message: str):
    """
    Logs a message using both deployer and orchestrator loggers.
    """
    logger1.info(f"Deployer: {message}")
    logger2.info(f"Orchestrator: {message}")

    logger1.debug(f"Deployer debug: {message}")
    logger2.debug(f"Orchestrator debug: {message}")

    logger1.error(f"Deployer error: {message}")
    logger2.error(f"Orchestrator error: {message}")

if __name__ == "__main__":
    log_message("This is a test message for both deployer and orchestrator loggers.")
    print("Messages logged successfully.")