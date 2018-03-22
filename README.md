# favro-export
A tool to export data from Favro in JSON format.

### What is Favro?

Favro is a beautiful task planning and collaboration app produced by Favro AB (Uppsala - Sweden). Favro's flexibility allows many styles of planning, such as Scrum and Kanban: I use it as the main tool of my Personal Kanban/GTD setup.

See https://www.favro.com/product to learn more about this app.

### Why this tool?

I wrote this tool to have my own backup of all data I enter in Favro, and to have an offline copy of the data in case I decide to migrate to another app someday.

The tool saves data into JSON files, replicating the logical structure of the entities in the Favro API (https://favro.com/developer/). In the destination directory, after a successful export operation, you will find several JSON files with:

* the organizations you have defined in Favro
* all users belonging to those organizations
* the collections
* the boards (widgets) in each collection, along with the columns for each board
* all cards with task lists, task, comments and attachments

### Usage instructions:

You need to have Java 8 installed; if you don't have it installed already, you may download the Oracle JDK from http://java.sun.com .

Then, use a text editor to prepare a file with the following contents:

    favro.base.url = https://favro.com/api/v1
    favro.user = _your favro username_
    favro.api.token = _your favro api token_

Execute the exporter, specifying the configuration file created in the above step and a destination directory where the exported data will be written:

    java -jar favro-exporter.jar --destination=favro-backup --configuration=favro.conf

You may use either relative or absolute paths for the destination and configuration parameter (relative path are considered relative to the current directory).

### Disclaimer

Favro is a product of FAVRO AB, Uppsala (Sweden): I'm in no way associated with FAVRO AB, and the use of the "Favro" name is for informative purposes - no copyright infringement is intended.

Please do not ask Favro's support for help with this tool.
