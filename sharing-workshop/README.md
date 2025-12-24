# Workshop Repository

This repository is used for storing and managing workshop materials. It serves as a central place for all documentation, exercises, and other resources related to workshops.

## Folder Structure

### docs/ 
This directory contains detailed documentation, guides, and other written materials for the workshops. This is where you'll find all the slides and notes. The materials here are published by [remarkjs](https://github.com/remarkjs/remark)

How to use:
1. Navigate to the `docs/slides/` directory
2. In `index.html`: add your slides as the following format:

```js
const slides = [
            {
                id: "vault",    // This should match the folder name under `docs/slides/`
                title: "Vault Workshop - 101",
                description: "A beginner's guide to understanding HashiCorp Vault."
            },
        ];
```
3. Create a new folder under `docs/slides/` with the same name as the `id` you provided in step 2.
4. Add your `index.html` file in the newly created folder, in which you will include all your markdown files under `sourceUrls`:
```js
sourceUrls = [
            'vault-0.md',   // Your markdown files here
            'vault-1.md',
            ...
        ]
```
4. Add your slide files in the newly created folder. Each slide can be a separate Markdown file, or in the single file and separate slides with `---` (three dashes).
5. Run the Nginx server using Docker Compose to serve the slides, if successful, access the slides at `http://localhost:8088` to see the list of available workshops.

### To be continued...
