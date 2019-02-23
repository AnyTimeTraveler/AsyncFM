This project is very much still work in progress.
Currently scanning and viewing already works.

# AsyncFM

A filemanager that takes an image of your file structure.

Then manipulate that image with no actual data being moved.

This means that you can take this image to another computer and edit it there.

This also means that copying and moving will take time and undo and redo is no problem. 

And then you can take that image back and apply the changes made from the image to the actual data.


The intended use is to create an image of a large server containing terabytes of backups,
then move the image file (megabytes in size) to a computer and use a gui to move data around.
Then take the transactions back to the server and let it move the files over night.
  

## Progress

 * Implement Transactions
 * Implement Delete
 * Implement Copy
 * Implement Move
 * Implement Rename
 * Implement Create Directory
 * Double clicking a directory in the table, should update the tree
 * Allow multiple selection
 * Add search

## Licenses

### Icons

Creative Commons Attribution 4.0 International license

### Filemanager GUI

Copyright 2015 Valentyn Kolesnikov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
