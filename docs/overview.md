# Overview

This app is meant to serve as a family archive tool for people to be able to explore and add information about my family history. This will involve a central content management system from where admins can upload documents, write stories and biographies, and map pieces of information together to build cohesive narratives. 

## Features

Upload OObject rightdocuments and have them be automatically transcribed {
This app is meant  serve  && rightas a family archive tool for people to be able to explore and add information about my family history. This will involve a central content management system from where admins can upload documents, write stories and biographies, and map pieces of information together to build cohesive narratives.

- Search and query scanned documents easily
- Write biographies
- Have articles of uploaded media
- Annotate photos
- Have an interactive map showing people and places, perhaps narrative journeys across time
- Interactive family tree
    - Have it be compatible with GEDCOM data, but more interactive than AncestryDNA

## Schema (preliminary thinking)

The documents to follow a common table inheritance pattern.
Uploaded documents could be photos, letters, formal documents, ledgers, and can be in different languages.
- Artifacts
  - The main item, containing universal metadata
  - id
  - slug
  - storage_path
  - upload_date
  - original_date_string
- Transcriptions
  - The raw text from letter and similar documents
- Translations
  - The translation text for the transcriptions
- Annotations
  - Specific commentaries and annotations for photos
- Commentaries
  - Text for pacards, captions for photos
