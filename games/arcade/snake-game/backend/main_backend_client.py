import httpx
import os
from dotenv import load_dotenv
from typing import Optional, Dict, Any
from jose import jwt, JWTError

load_dotenv()

MAIN_BACKEND_URL = os.getenv("MAIN_BACKEND_URL", "http://localhost:8080")
JWT_SECRET_KEY = os.getenv("JWT_SECRET_KEY", "your-super-secret-key-change-in-production")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")

class MainBackendClient:
    """Client for communicating with the main Java backend"""

    def __init__(self):
        self.base_url = MAIN_BACKEND_URL
        self.timeout = httpx.Timeout(10.0)

    def verify_jwt_token(self, token: str) -> Optional[Dict[str, Any]]:
        """
        Verify JWT token and extract user information
        Returns: dict with user_id and email, or None if invalid
        """
        try:
            # Remove "Bearer " prefix if present
            if token.startswith("Bearer "):
                token = token[7:]

            payload = jwt.decode(token, JWT_SECRET_KEY, algorithms=[JWT_ALGORITHM])

            email = payload.get("sub")
            if not email:
                return None

            return {
                "user_id": None,  # Will be fetched from backend
                "email": email,
                "token": token
            }
        except JWTError:
            return None

    async def get_user_info(self, user_token: str) -> Optional[Dict[str, Any]]:
        """
        Get user information from main backend using token
        Returns: dict with userId, email, balance etc., or None if failed
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}/api/user/profile",
                    headers={
                        "Authorization": f"Bearer {user_token}",
                        "Content-Type": "application/json"
                    }
                )

                if response.status_code == 200:
                    return response.json()
                else:
                    return None

        except Exception:
            return None

    async def deduct_balance(
        self,
        user_token: str,
        amount: float,
        game_type: str = "SNAKE"
    ) -> tuple[bool, Optional[str]]:
        """
        Deduct balance from user's account
        Returns: (success, error_message)
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    f"{self.base_url}/api/user/wallet/deduct",
                    json={
                        "amount": amount,
                        "gameType": game_type
                    },
                    headers={
                        "Authorization": f"Bearer {user_token}",
                        "Content-Type": "application/json"
                    }
                )

                if response.status_code == 200:
                    return True, None
                elif response.status_code == 400:
                    data = response.json()
                    return False, data.get("error", "Insufficient balance")
                else:
                    return False, "Failed to deduct balance"

        except httpx.TimeoutException:
            return False, "Backend timeout"
        except Exception as e:
            return False, f"Backend error: {str(e)}"

    async def add_balance(
        self,
        user_token: str,
        amount: float,
        game_type: str = "SNAKE"
    ) -> tuple[bool, Optional[str]]:
        """
        Add winnings to user's account
        Returns: (success, error_message)
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    f"{self.base_url}/api/user/wallet/add",
                    json={
                        "amount": amount,
                        "gameType": game_type
                    },
                    headers={
                        "Authorization": f"Bearer {user_token}",
                        "Content-Type": "application/json"
                    }
                )

                if response.status_code == 200:
                    return True, None
                else:
                    return False, "Failed to add balance"

        except httpx.TimeoutException:
            return False, "Backend timeout"
        except Exception as e:
            return False, f"Backend error: {str(e)}"

    async def get_user_balance(self, user_token: str) -> Optional[float]:
        """
        Get user's current balance
        Returns: balance amount or None if failed
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.get(
                    f"{self.base_url}/api/wallet/balance",
                    headers={
                        "Authorization": f"Bearer {user_token}",
                        "Content-Type": "application/json"
                    }
                )

                if response.status_code == 200:
                    data = response.json()
                    return data.get("balance")
                else:
                    return None

        except Exception:
            return None

    async def record_game_session(
        self,
        user_token: str,
        game_type: str,
        bet_amount: float,
        win_amount: float,
        session_token: str
    ) -> bool:
        """
        Record game session in main backend for history/statistics
        Returns: True if successful
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                response = await client.post(
                    f"{self.base_url}/api/games/record",
                    json={
                        "gameType": game_type,
                        "betAmount": bet_amount,
                        "winAmount": win_amount,
                        "sessionToken": session_token
                    },
                    headers={
                        "Authorization": f"Bearer {user_token}",
                        "Content-Type": "application/json"
                    }
                )

                return response.status_code == 200

        except Exception:
            # Don't fail the game if recording fails
            return False
