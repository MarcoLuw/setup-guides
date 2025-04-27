import { useState } from 'react';
import UsernamePage from './component/UsernamePage.jsx';
import ChatPage from './component/ChatPage.jsx';

function App() {
  const [username, setUsername] = useState(null);

  return (
    <div>
      {username ? <ChatPage username={username} /> : <UsernamePage setUsername={setUsername} />}
    </div>
  );
}

export default App;