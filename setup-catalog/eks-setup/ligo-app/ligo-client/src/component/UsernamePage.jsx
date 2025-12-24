import { useState } from "react";
import { Button, TextField, Container, Box, Typography } from "@mui/material";
import PropTypes from "prop-types";

function UsernamePage({ setUsername }) {
  UsernamePage.propTypes = {
    setUsername: PropTypes.func.isRequired,
  };

  const [inputUsername, setInputUsername] = useState("");

  const handleUsernameSubmit = (event) => {
    event.preventDefault();
    if (inputUsername) {
      setUsername(inputUsername);
    }
  };

  return (
    <Container
      maxWidth="xs"
      sx={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Box
        bgcolor="white"
        borderRadius={3}
        boxShadow={3}
        p={4}
        textAlign="center"
        maxWidth={{ xs: "90%", sm: "400px" }}
      >
        {/* Dòng tiêu đề lớn */}
        <Typography
          variant={{ xs: "h4", sm: "h3" }}
          fontWeight="bold"
          color="#A50034"
          mb={2}
          display="block"
        >
          Welcome to LigoChat!
        </Typography>

        {/* Dòng hướng dẫn nhỏ hơn */}
        {/* <Typography
          variant={{ xs: "body1", sm: "h6" }}
          color="#6B6B6B"
          mb={3}
          display="block"
        >
          Please enter your username
        </Typography> */}

        {/* Form nhập username */}
        <form onSubmit={handleUsernameSubmit}>
          <Box display="flex" flexDirection={{ xs: "column", sm: "row" }} alignItems="center">
            <TextField
              fullWidth
              sx={{
                "& .MuiOutlinedInput-root": {
                  borderRadius: "24px",
                  borderColor: "#A50034",
                  "& fieldset": { borderColor: "#A50034" },
                  "&:hover fieldset": { borderColor: "darkred" },
                  "&.Mui-focused fieldset": { borderColor: "darkred" },
                },
                mb: { xs: 2, sm: 0 }, // Khoảng cách trên mobile
              }}
              placeholder="Enter your username"
              value={inputUsername}
              onChange={(e) => setInputUsername(e.target.value)}
            />
            <Box ml={{ xs: 0, sm: 2 }} width={{ xs: "100%", sm: "auto" }}>
              <Button
                variant="contained"
                fullWidth
                sx={{
                  bgcolor: "#A50034",
                  color: "white",
                  borderRadius: "24px",
                  paddingX: 3,
                  paddingY: 1,
                  "&:hover": { bgcolor: "darkred" },
                }}
                type="submit"
              >
                Enter
              </Button>
            </Box>
          </Box>
        </form>
      </Box>
    </Container>
  );
}

export default UsernamePage;
