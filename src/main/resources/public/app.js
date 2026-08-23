let ws;
let mySymbol = "";
let myTurn = false;
let selectedPieceIndex = null;

function initWebSocket() {
    const protocol = location.protocol === "https:" ? "wss://" : "ws://";
    ws = new WebSocket(`${protocol}${location.host}/ws`);

    ws.onopen = () => {
        document.getElementById("statusMessage").innerText = "Connected to the server. Ready to play!";
    };

    ws.onmessage = (event) => {
        const packet = JSON.parse(event.data);
        handlePacket(packet);
    };

    ws.onclose = () => {
        document.getElementById("statusMessage").innerText = "Disconnected from the server.";
    };
}

function handlePacket(packet) {
    const cmd = packet.command;
    const content = packet.content;
    const status = document.getElementById("statusMessage");

    if (cmd === "SYSTEM") {
        status.innerText = content;
    } else if (cmd === "GAME_START") {
        const [symbol, opponent] = content.split("|");
        mySymbol = symbol;
        document.getElementById("lobbySection").style.display = "none";
        document.getElementById("gameSection").style.display = "block";
        document.getElementById("roleInfo").innerText = `You play as [ ${mySymbol} ] vs ${opponent}`;
        clearBoardUI();
    } else if (cmd === "TURN") {
        myTurn = true;
        status.innerText = content;
        status.style.color = "#4ade80";
    } else if (cmd === "WAIT") {
        myTurn = false;
        clearSelectedPiece();
        status.innerText = content;
        status.style.color = "#94a3b8";
    } else if (cmd === "PIECE_SELECTED") {
        setSelectedPiece(Number(content));
        status.innerText = "Piece selected. Choose an empty square to move it to.";
    } else if (cmd === "PIECE_DESELECTED") {
        clearSelectedPiece();
        status.innerText = "Select a piece to move.";
    } else if (cmd === "BOARD_UPDATE") {
        const [index, symbol] = content.split(",");
        const cell = document.getElementById(`cell-${index}`);
        
        // If the cell is already occupied, ignore the update.
        cell.innerText = symbol || ""; 
        if (!symbol && Number(index) === selectedPieceIndex) clearSelectedPiece();
        if (symbol === "X") cell.style.color = "#38bdf8";
        else if (symbol === "O") cell.style.color = "#f43f5e";
        
    } else if (cmd === "GAME_OVER") {
        myTurn = false;
        status.innerText = content;
        status.style.color = "#facc15";
        document.getElementById("btnRestart").style.display = "block";
    } else if (cmd === "ERROR") {
        clearSelectedPiece();
        alert(content);
    }
}

function searchMatch() {
    const usernameInput = document.getElementById("usernameInput");
    const name = usernameInput.value.trim() || "Player";
    ws.send(JSON.stringify({ command: "PLAY", content: "", sender: name }));
    document.getElementById("btnPlay").disabled = true;
}

function cellClicked(index) {
    // If it's not the player's turn, ignore the click.
    if (!myTurn) return;
    
    const cell = document.getElementById(`cell-${index}`);
    if (cell.innerText === mySymbol && selectedPieceIndex !== index) {
        setSelectedPiece(index);
    }

    // Send the move or selection command to the server.
    ws.send(JSON.stringify({ command: "MOVE", content: index.toString(), sender: "" }));
}

function setSelectedPiece(index) {
    clearSelectedPiece();
    selectedPieceIndex = index;
    document.getElementById(`cell-${index}`).classList.add("selected");
}

function clearSelectedPiece() {
    if (selectedPieceIndex !== null) {
        document.getElementById(`cell-${selectedPieceIndex}`).classList.remove("selected");
        selectedPieceIndex = null;
    }
}

function clearBoardUI() {
    clearSelectedPiece();
    for (let i = 0; i < 9; i++) {
        document.getElementById(`cell-${i}`).innerText = "";
    }
    document.getElementById("btnRestart").style.display = "none";
}

function resetToLobby() {
    document.getElementById("gameSection").style.display = "none";
    document.getElementById("lobbySection").style.display = "block";
    document.getElementById("btnPlay").disabled = false;
    document.getElementById("statusMessage").innerText = "Ready to search for another match.";
}

window.onload = initWebSocket;
