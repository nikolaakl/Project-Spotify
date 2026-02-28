# 🎶 Spotify – Client-Server Music Streaming Platform

A Java-based client–server application that simulates a simplified version of Spotify, supporting real-time audio streaming over the network.

The system is built using Java NIO (SocketChannel) for communication and javax.sound.sampled (SourceDataLine) for real-time audio playback. It follows a modular architecture with command-based request handling and structured server responses.

## 🚀 Overview

This project implements a distributed music streaming platform consisting of:

🖥 Spotify Server

💻 Spotify CLI Client

The client communicates with the server using a custom text-based protocol.
Audio files are streamed in real time without being fully downloaded beforehand.

## 🏗 Architecture Highlights

- Client–Server architecture (Java NIO)

- Command Pattern for request handling

- Structured server responses with status codes

- Real-time streaming using SourceDataLine

- Persistent storage using files (users & playlists)

- Error logging with user-friendly console messages

- Session-based authentication

## 🎵 Server Features

- User registration (email & password)

- User login

- Persistent user storage (file-based)

- Song repository (.wav files only)

- Search songs by title or artist

- Track and maintain statistics for most played songs

- Create and manage playlists (file-based storage)

- Add songs to playlists

- Retrieve playlist details

- Stream songs in real time

- Structured error handling & logging

## 💻 Client Commands (CLI)
- register \<email> \<password>
- login \<email> \<password>
- logout
- disconnect
- search \<words>
- top \<number>
- create-playlist \<playlist_name>
- add-song-to \<playlist_name> \<song>
- show-playlist \<playlist_name>
- play \<song>
- stop

## 🔊 Real-Time Streaming

The streaming mechanism:

1. The server reads the .wav file.

2. It sends audio format metadata to the client.

3. The client reconstructs AudioFormat.

4. A SourceDataLine is created dynamically.

5. Audio bytes are streamed and played in real time.

## 🛡 Error Handling

- User-friendly error messages in the console

- Technical errors and stack traces logged to file
