# Copyright (C) 2019-present Eiichiro Uchiumi and the Prodigy Authors. 
# All Rights Reserved.
import json
import logging
import os
import sys
import urllib.request

def main():
    """The main method sends '/restock' request for the initialization.

    Environment variable 'PRODIGY_SUSHI_ENDPOINT' must be pointed to Prodigy 
    Sushi API endpoint URL that 'mvn deploy' output.
    """
    logging.basicConfig(format='%(asctime)s %(levelname)-8s %(message)s', level=logging.INFO)
    endpoint = os.getenv('PRODIGY_SUSHI_ENDPOINT')
    
    if not endpoint:
        logging.error("Environment variable 'PRODIGY_SUSHI_ENDPOINT' must be set")
        sys.exit(1)

    try: 
        with urllib.request.urlopen(endpoint + '/restock', data=b'') as res:
            logging.info("Items have been restocked successfully")
            sys.exit(0)
    except urllib.error.HTTPError as e:
        logging.error("Failed to restock items")
        sys.exit(1)
    except urllib.error.URLError as e:
        logging.error("Unable to connect to " + endpoint)
        sys.exit(1)

if __name__ == '__main__':
    main()
