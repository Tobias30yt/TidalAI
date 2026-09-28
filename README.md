TidalAI

TidalAI is a local AI workspace for running and managing AI-assisted tasks directly on your computer.

The application is designed around local AI providers, multiple specialized agents, project management, persistent conversations, and development tools.

Features

* Local AI chat
* LM Studio integration
* Local model selection
* Multiple specialized AI agents
* Main, Reasoning, Coder, Researcher, Tester and Reviewer agents
* Agent-specific model assignment
* Task management
* Project management
* Persistent chats and tasks
* Local workspace configuration
* System monitoring
    * CPU usage
    * RAM usage
    * GPU utilization
    * VRAM usage
    * GPU temperature
    * GPU power usage
* Dark and light themes
* Fullscreen mode with F11
* Code-oriented AI workflows
* Local file and project tools
* Configurable tool approval modes
* Windows and Linux releases

Requirements

Windows

Recommended:

* Windows 10 or newer
* x64 CPU
* Java runtime included with the packaged application
* LM Studio for local AI models

A dedicated Java installation is not required when using the packaged Windows application.

Linux

Recommended:

* 64-bit Linux
* x86_64 CPU
* The AppImage includes its own Java runtime
* LM Studio for local AI models

The current Linux release is built for x86_64 systems.

LM Studio

TidalAI currently uses LM Studio as its local AI backend.

Install LM Studio separately and make sure its local server is running before using AI features.

TidalAI communicates with the LM Studio API locally. If LM Studio is not running, TidalAI can still start, but AI requests will not work until the backend is available.

The default LM Studio API is typically available at:

http://localhost:1234

TidalAI automatically checks the local LM Studio connection.

First Start

1. Start TidalAI.
2. Install and open LM Studio if it is not already installed.
3. Download a compatible model in LM Studio.
4. Start the LM Studio local server.
5. Open TidalAI.
6. Open Models and refresh the available models.
7. Select the models you want to use.
8. Start a chat or create a task.

Windows

The Windows release contains a packaged Java runtime, so Java does not need to be installed separately.

The release contains:

TidalAI-0.1.0.exe

The installer creates a normal Windows installation and can create a Start Menu entry and desktop shortcut.

Building the Windows Release

The Windows release is built using jpackage.

Required:

* JDK 27
* Maven
* WiX Toolset

From the project directory:

.\packaging\package.bat

The Windows release is generated in:

release\windows\

The resulting files include:

TidalAI-0.1.0.exe
TidalAI\

Linux

The Linux release is distributed as an AppImage.

The AppImage contains the Java runtime required by TidalAI and does not require a separate Java installation.

Run:

chmod +x ./release/linux/TidalAI-0.1.0.AppImage
./release/linux/TidalAI-0.1.0.AppImage

Building the Linux AppImage

The Linux release can be built using WSL2 or a native Linux environment.

Required:

* JDK 27
* Maven
* curl
* x86_64 Linux
* jpackage
* appimagetool

Run:

./packaging/package-linux.sh

The resulting AppImage is created at:

release/linux/TidalAI-0.1.0.AppImage

Project Structure

TidalAI/
├── packaging/
│   ├── make-icon.ps1
│   ├── package.bat
│   └── package-linux.sh
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/tidalai/
│       │       ├── agents/
│       │       ├── chat/
│       │       ├── config/
│       │       ├── models/
│       │       ├── monitor/
│       │       ├── persistence/
│       │       ├── projects/
│       │       ├── providers/
│       │       ├── tasks/
│       │       ├── views/
│       │       ├── Dashboard.java
│       │       ├── Main.java
│       │       └── Theme.java
│       │
│       └── resources/
│           └── iconig.png
│
├── pom.xml
└── README.md

Agent System

TidalAI uses several specialized agents.

Main Agent

Coordinates the overall task.

Reasoning Agent

Analyzes the request and creates a structured execution plan.

Coder Agent

Handles programming and file modification tasks.

Researcher Agent

Collects and analyzes information from the project workspace.

Tester Agent

Runs tests and development commands where permitted.

Reviewer Agent

Reviews generated work and identifies problems that may need correction.

The model used by each agent can be configured independently.

Workspace

TidalAI uses a configurable workspace for project files.

File operations performed through the application are restricted to the configured workspace.

Depending on the configured approval mode, actions can either require confirmation or be executed automatically.

Configuration

TidalAI stores configuration and persistent data in:

~/.tidalai/

The configuration file is:

~/.tidalai/config.json

Persistent application data is stored in:

~/.tidalai/data/

This includes information such as:

* Projects
* Tasks
* Agents
* Chats

System Monitoring

The dashboard can display local system information including:

CPU
RAM
GPU
VRAM
GPU temperature
GPU power usage

On NVIDIA systems, TidalAI uses nvidia-smi when available to retrieve GPU information.

If NVIDIA GPU information is unavailable, the application continues running without GPU statistics.

Security and Permissions

TidalAI is designed for local development and AI-assisted workflows.

Some operations, such as running commands or modifying project files, can require user approval depending on the configured settings.

The command execution system applies restrictions to supported commands.

Important: the terminal execution feature is not a complete operating-system sandbox. Commands executed through the terminal may have access beyond the TidalAI workspace.

Only run commands you trust.

Troubleshooting

LM Studio is not connected

Make sure:

1. LM Studio is installed.
2. LM Studio is running.
3. A model is available.
4. The LM Studio local server is running.
5. The configured API address is reachable.

If TidalAI shows:

LM Studio connected: false

the local backend is currently unavailable.

GPU information is missing

For NVIDIA GPUs, check whether:

nvidia-smi

works on the system.

If it does not work, TidalAI may not be able to display GPU statistics.

Linux AppImage does not start

Make the file executable:

chmod +x TidalAI-0.1.0.AppImage

Then run:

./TidalAI-0.1.0.AppImage

Windows EXE build requires WiX

Creating a Windows installer with jpackage requires a supported WiX Toolset installation.

The Windows build script therefore requires WiX when creating the .exe installer.

Development

Clone or copy the project and make sure Java 27 and Maven are available.

Run:

mvn clean javafx:run

On Windows PowerShell, if necessary:

$env:JAVA_HOME="C:\Program Files\Java\jdk-27"
mvn clean javafx:run

Version

Current release:

TidalAI 0.1.0

This is an early release and the project is still under active development.

License

No final open-source license has been selected for TidalAI yet.

Until a license is added to the repository, all rights are reserved by the project author.
