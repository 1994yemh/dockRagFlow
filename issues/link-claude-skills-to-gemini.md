# Task: Link Claude Skills to Gemini

## Context
The user has installed many skills under `C:\Users\yemh\.claude\skills` and wants to reuse them in the Gemini IDE (Antigravity).

## Plan
1. Create a directory junction in `C:\Users\yemh\.gemini\antigravity-ide\skills` pointing to `C:\Users\yemh\.claude\skills`.
2. Create a backup directory junction in `C:\Users\yemh\.gemini\config\skills` pointing to `C:\Users\yemh\.claude\skills`.
3. Verify that the junctions are working and that Gemini can read the contents.
