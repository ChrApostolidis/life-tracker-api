# Demo media

Recordings referenced by the main `README.md`'s [Demos](../../README.md#demos) section. Nothing here yet — this is where the files go once recorded.

## Format

These are terminal recordings, not UI screen captures — a tool built for that (e.g. [`asciinema`](https://asciinema.org/) + [`agg`](https://github.com/asciinema/agg) to render an asciinema cast to GIF, or [`vhs`](https://github.com/charmbracelet/vhs) to script the whole recording as a reproducible `.tape` file) will look far cleaner than a raw screen capture of a terminal window — no font-rendering artifacts, crisp text at any GIF compression level, and a `vhs` `.tape` script can be committed alongside the GIF so the recording is reproducible.

- **GIF, not video, for anything embedded via `![alt](path)`** — GitHub's markdown renderer autoplays `.gif` inline but does not render `<video>` tags or play `.mp4`/`.mov` from a relative path.
- **For a long clip** (the full `./mvnw test` run can take a while): either speed it up in the GIF encode, trim to just the start and the final summary line, or drag the raw video into a GitHub PR/issue/README edit box on github.com instead — it uploads to `https://github.com/<org>/<repo>/assets/...` and gives you a URL that autoplays inline in rendered markdown.
- Name files exactly as referenced in the main README's Demos table (`cold-boot.gif`, `recurrence-expansion.gif`, `test-suite.gif`, `habit-hard-delete.gif`) so the existing links resolve without further edits.

## Recording tips

- Use a clean checkout for the cold-boot clip specifically — the point is showing Flyway apply every migration from scratch, which won't happen against a DB file that already exists.
- For the recurrence-expansion clip, keep both `curl` commands and their JSON responses on screen long enough to actually read — cut for time elsewhere, not there.
- Pipe test output through something that keeps the pass/fail summary visible at the end rather than scrolling it off — that final line is the payoff of the clip.
