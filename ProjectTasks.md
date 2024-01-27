# Provider app

## Required

- [x] Store contents of albums (title and artist)
    - [x] Source: Database or file storage
- [x] API for other apps to access data
    - (built into android)
- [x] Subscription on api retrieval
    - [x] when data changes, notify subscribers

## Optional

- View subscriber apps
- [x] Force update all subscriber apps to pull new data
    - (sorta, via the data generator in the provider app)
    - [ ] "invalidate all and reload"
- [ ] Display transaction history
    - data is logged, but not displayed

## Unknown

- [ ] View (read-only) of the stored data
- [ ] Album/ artist include art (pictures)
- [ ] Remove a subscriber and optionally force invalidate of data

# Editor App

## Required

- [x] Request data from provider app
    - [x] App holds contents in cache only (optional, using objectbox database)
    - [x] Provider holds persistent storage
- [x] Display data in a list view
    - [x] As albums
- [x] User can edit (add/ remove/ modify) contents

## Optional

- [ ] Force refresh of data
    - [ ] show "last updated: xyz"
- [ ] Auto-update every x mins
    - [x] (not needed, it's on auto-reload)
    - Low priority
- [ ] Sort albums
- [ ] More animations
  - swipe left/ right for action controls
  - swipe down to refresh (gives the user a sense of control)
- [ ] Request status (exists, locked, errors, etc) from provider app
- [ ] Change view as "list of artists"
    - [ ] with collapsable album sub-lists
- [ ] While editing data, show suggestions of other artists
    - note: review "duplicate resolution" option first
- [ ] Second editor app
    - allows viewing of data which the other editor has updated
    - show history of changes, instead of mirror copy of "editor #1"
- [ ] Locking of data (editing)
    - Artist/ albums
        - Album can't be transferred when locked
        - Artist (and contents) can't edited when locked
    - [ ] time-based
    - [ ] password-based

## Unknown

- When the provider sends a notify to subscribers, editor should:
    - [x] a) Show a "New data! Click to reload" footer button
    - b) Force reload view
        - Could be annoying if many large changes happen
    - c) "disable" items which no longer exist from the provider app
        - and show "reload data" footer button
- Resolving duplicates
    - a) Persist duplicate content (and show a icon "duplicate data")
        - might get annoying if the intent was to merge items
    - b) Offer to merge duplicate content ("merge"/ "cancel")
    -[x] c) Overwrite other/s contents (native "move item")
        - [x] deny adding if the name already exists
        - [ ] Show "merge data?" prompt instead of just merging

