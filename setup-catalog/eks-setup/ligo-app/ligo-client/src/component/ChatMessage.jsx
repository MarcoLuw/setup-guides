import Avatar from "react-avatar";
import { Box, Typography } from "@mui/material";
import PropTypes from "prop-types";

function ChatMessage({ message, username }) {
  ChatMessage.propTypes = {
    message: PropTypes.object.isRequired,
    username: PropTypes.string.isRequired,
  };

  // Xử lý tin nhắn CONNECT/DISCONNECT
  if (message.type === "CONNECT" || message.type === "DISCONNECT") {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          width: "100%",
          my: 1,
        }}
      >
        <Typography
          sx={{ color: message.type === "CONNECT" ? "lime" : "orangered" }}
        >
          {message.sender + " " + message.type.toLowerCase() + "ed"}
        </Typography>
      </Box>
    );
  }

  const isCurrentUser = message.sender === username;

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: isCurrentUser ? "flex-end" : "flex-start",
        my: 1,
      }}
    >
      {/* Avatar + Tên người gửi */}
      <Box
        sx={{
          display: "flex",
          flexDirection: isCurrentUser ? "row-reverse" : "row",
          alignItems: "center",
          gap: 1,
        }}
      >
        <Avatar name={message.sender} size="35" round={true} />
        <Typography fontWeight="bold">{message.sender}</Typography>
      </Box>

      {/* Nội dung tin nhắn */}
      <Box
        sx={{
          mt: 0.5,
          p: 2,
          borderRadius: 2,
          maxWidth: { xs: "80%", sm: "60%" },
          wordWrap: "break-word",
          bgcolor: isCurrentUser ? "#e02f6d" : "#6B6B6B",
          color: "white",
        }}
      >
        <Typography>{message.content}</Typography>
      </Box>
    </Box>
  );
}

export default ChatMessage;
