import { useState, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs";
import ChatMessage from "./ChatMessage.jsx";
import {
  Button,
  TextField,
  Container,
  Box,
  Select,
  MenuItem,
  Typography,
} from "@mui/material";
import PropTypes from "prop-types";

function ChatPage({ username }) {
  ChatPage.propTypes = {
    username: PropTypes.string.isRequired,
  };

  const [messages, setMessages] = useState([]);
  const [client, setClient] = useState(null);
  const [connectionStatus, setConnectionStatus] = useState("Connecting...");
  const [translationMode, setTranslationMode] = useState("none");
  const [grammarCheckedText, setGrammarCheckedText] = useState("");
  const [askLigoBotApi, setAskLigoBotApi] = useState("");
  const messageInputRef = useRef();
  const messagesEndRef = useRef(null);
  const [isGrammarCheckedVisible, setIsGrammarCheckedVisible] = useState(true);
  const [isAskLigoBotVisible, setIsAskLigoBotVisible] = useState(true);
  const BACKEND_URL = "http://a0cd0ecf534dc4c7eaf06493f9ebf310-1479717945.ap-northeast-2.elb.amazonaws.com/chat"

  useEffect(() => {
    const newClient = new Client({
      webSocketFactory: () => new SockJS(`${BACKEND_URL}/ws`),
      onConnect: () => {
        const joinMessage = { sender: username, type: "CONNECT" };
        newClient.publish({
          destination: "/app/chat.add-user",
          body: JSON.stringify(joinMessage),
        });

        newClient.subscribe("/topic/public", (message) => {
          const newMessage = JSON.parse(message.body);
          setMessages((prevMessages) => [...prevMessages, newMessage]);
        });

        newClient.subscribe("/user/queue/private/checkgrammar", (message) => {
          const newMessage = JSON.parse(message.body);
          setGrammarCheckedText(newMessage.content);
        });

        newClient.subscribe("/user/queue/private/askligobot", (message) => {
          const newMessage = JSON.parse(message.body);
          setAskLigoBotApi(newMessage.content);
        });

        setConnectionStatus("Connected");
      },
      onDisconnect: () => {
        setConnectionStatus("Disconnected");
      },
      onWebSocketClose: () => {
        setConnectionStatus("Disconnected");
      },
      onWebSocketError: () => {
        setConnectionStatus("Failed to connect");
      },
    });

    newClient.activate();
    setClient(newClient);

    return () => {
      newClient.deactivate();
    };
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const sendMessage = () => {
    if (messageInputRef.current.value && client) {
      const chatMessage = {
        sender: username,
        content: messageInputRef.current.value,
        type: "CHAT",
        translationMode: translationMode,
      };
      client.publish({
        destination: "/app/chat.send-message",
        body: JSON.stringify(chatMessage),
      });

      messageInputRef.current.value = "";
    }
  };

  const checkGrammar = () => {
    if (messageInputRef.current.value && client) {
      const grammarCheckMessage = {
        sender: username,
        content: messageInputRef.current.value,
        // type: "GRAMMAR_CHECK",
      };
      client.publish({
        destination: "/app/chat.check-grammar",
        body: JSON.stringify(grammarCheckMessage),
      });
      setIsGrammarCheckedVisible(true);
    }
  };

  const askLigoBot = () => {
    if (messageInputRef.current.value && client) {
      const askLigoBotMessage = {
        sender: username,
        content: messageInputRef.current.value,
        // type: "ASK_LIGOBOT",
      };
      client.publish({
        destination: "/app/chat.ask-ligobot",
        body: JSON.stringify(askLigoBotMessage),
      });
      setIsAskLigoBotVisible(true);
    }
  };

  return (
    <Container
      maxWidth="md"
      sx={{
        bgcolor: "white",
        height: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        padding: 2,
      }}
    >
      <Typography variant="h6" color="primary" mt={2}>
        {connectionStatus}
      </Typography>

      <Box
        sx={{
          flex: 1,
          width: "100%",
          maxWidth: "600px",
          overflowY: "auto",
          bgcolor: "#f9f9f9",
          p: 2,
          borderRadius: 2,
          mt: 2,
        }}
      >
        {messages.map((message, index) => (
          <ChatMessage key={index} message={message} username={username} />
        ))}
        <div ref={messagesEndRef} />
      </Box>

      {/* Nhập tin nhắn */}
      <Box
        sx={{
          display: "flex",
          flexDirection: { xs: "column", sm: "row" },
          gap: 2,
          width: "100%",
          maxWidth: "600px",
          mt: 2,
        }}
      >
        <TextField
          fullWidth
          inputRef={messageInputRef}
          placeholder="Type a message..."
          onKeyDown={(e) => e.key === "Enter" && sendMessage()}
          sx={{
            "& .MuiOutlinedInput-root": {
              borderRadius: "36px",
            },
          }}
        />

        <Button
          variant="contained"
          sx={{ bgcolor: "#A50034", borderRadius: "36px" }}
          onClick={sendMessage}
        >
          Send
        </Button>
      </Box>

      {/* Select mode + Check Grammar */}
      <Box
        sx={{
          display: "flex",
          flexDirection: { xs: "column", sm: "row" },
          gap: 2,
          width: "100%",
          maxWidth: "600px",
          mt: 2,
        }}
      >
        <Select
          value={translationMode}
          onChange={(e) => setTranslationMode(e.target.value)}
          sx={{
            width: { xs: "100%", sm: "160px" },
            height: "42px",
            borderRadius: "36px",
            bgcolor: "#A50034",
            color: "white",
            textAlign: "center",
            "& .MuiSelect-select": {
              display: "flex",
              justifyContent: "center",
            },
          }}
        >
          {[
            { value: "none", label: "Translation ❌" },
            { value: "ko", label: "Korean" },
            { value: "en", label: "English" },
            { value: "vi", label: "Vietnamese" },
          ].map((item) => (
            <MenuItem
              key={item.value}
              value={item.value}
              sx={{
                paddingY: 1.5,
                borderRadius: "12px",
                "&:hover": {
                  bgcolor: "#A50034",
                  color: "white",
                },
              }}
            >
              {item.label}
            </MenuItem>
          ))}
        </Select>


        <Button
          variant="contained"
          sx={{ bgcolor: "#A50034", borderRadius: "36px", width: "100%" }}
          onClick={checkGrammar}
        >
          Check Grammar
        </Button>

        <Button
          variant="contained"
          sx={{ bgcolor: "#A50034", borderRadius: "36px", width: "100%" }}
          onClick={askLigoBot}
        >
          Ask LigoBot
        </Button>
      </Box>

      {/* Kết quả kiểm tra ngữ pháp */}
      {grammarCheckedText && isGrammarCheckedVisible && (
        <Box
          sx={{
            mt: 2,
            p: 2,
            borderRadius: 2,
            bgcolor: "#2e2e2e",
            color: "white",
            width: "100%",
            maxWidth: "600px",
            position: "relative",
          }}
        >
          <Button
            onClick={() => setIsGrammarCheckedVisible(false)}
            sx={{
              position: "absolute",
              top: 0,
              right: 0,
              color: "white",
            }}
          >
            ❌
          </Button>
          <Typography>
            <strong>Checked:</strong> {grammarCheckedText}
          </Typography>
        </Box>
      )}

      {/* Kết quả ask LigoBot */}
      {askLigoBotApi && isAskLigoBotVisible && (
        <Box
          sx={{
            mt: 2,
            p: 2,
            borderRadius: 2,
            bgcolor: "#2e2e2e",
            color: "white",
            width: "100%",
            maxWidth: "600px",
            position: "relative",
          }}
        >
          <Button
            onClick={() => setIsAskLigoBotVisible(false)}
            sx={{
              position: "absolute",
              top: 0,
              right: 0,
              color: "white",
            }}
          >
            ❌
          </Button>
          <Typography>
            <strong>LigoBot:</strong> {askLigoBotApi}
          </Typography>
        </Box>
      )}
    </Container>
  );
}

export default ChatPage;
