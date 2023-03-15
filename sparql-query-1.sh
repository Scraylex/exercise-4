##!/bin/bash
comunica-sparql https://solid.interactions.ics.unisg.ch/lukas/profile/card\#me \
    "SELECT ?person WHERE { ?p foaf:knows ?person. }"