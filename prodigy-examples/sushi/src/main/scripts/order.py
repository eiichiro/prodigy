# Copyright (C) 2019-present Eiichiro Uchiumi and the Prodigy Authors. 
# All Rights Reserved.
from enum import Enum
import json
import logging
import os
import random
import sys
import time
import urllib.request

class Item(Enum):
    """Item provides sushi item constants."""
    SALMON = 'salmon'
    YELLOWTAIL = 'yellowtail'
    TUNA = 'tuna'
    MEDIUM_FATTY_TUNA = 'medium fatty tuna'
    SHRIMP = 'shrimp'
    SALMON_ROE = 'salmon roe'
    SQUID = 'squid'
    SCALLOP = 'scallop'
    SEA_URCHIN = 'sea urchin'
    HORSE_MACKEREL = 'horse mackerel'
    CUCUMBER_ROLL = 'cucumber roll'

def main():
    """The main method sends '/order' request for randomly selected Item every 
    1 second.

    Environment variable 'PRODIGY_SUSHI_ENDPOINT' must be pointed to Prodigy 
    Sushi API endpoint URL that 'mvn deploy' output.
    """
    logging.basicConfig(format='%(asctime)s %(levelname)-8s %(message)s', level=logging.INFO)
    endpoint = os.getenv('PRODIGY_SUSHI_ENDPOINT')
    
    if not endpoint:
        logging.error("Environment variable 'PRODIGY_SUSHI_ENDPOINT' must be set")
        sys.exit(1)
    
    items = list(Item)
    
    while True:
        item = items[random.randint(0, len(items) - 1)]
        data = {
            'name': item.value
        }

        try: 
            with urllib.request.urlopen(endpoint + '/order', data=json.dumps(data).encode('utf-8')) as res:
                served = json.loads(res.read().decode('utf-8'))
                logging.info("{} {} served".format(served[item.value], item.value))
        except urllib.error.HTTPError as e:
            logging.warning(e.read().decode('utf-8'))
        except urllib.error.URLError as e:
            logging.error("Unable to connect to " + endpoint)
            sys.exit(1)
        
        time.sleep(1)
        
if __name__ == '__main__':
    main()
