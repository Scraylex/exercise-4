##!/bin/bash
comunica-sparql https://solid.interactions.ics.unisg.ch/lukas/profile/card\#me \
   "SELECT ?name WHERE { ?person foaf:knows ?friend . ?friend foaf:knows ?foaf . ?foaf foaf:name ?name . FILTER (?person = <https://solid.interactions.ics.unisg.ch/lukas/profile/card#me>) }"