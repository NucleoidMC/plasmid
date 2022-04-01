use std::collections::HashSet;
use std::time::Duration;

use glam::IVec3;
use xtra::Address;

use plasmid::{Command, GetParticipants, Plasmid, PlayerRef};

const AIR: &str = "air";
const SNOW_BLOCK: &str = "snow_block";
const BEDROCK: &str = "bedrock";

const DIAMOND_SHOVEL: &str = "diamond_shovel";

#[tokio::main]
async fn main() {
    let plasmid = Plasmid::connect().await;

    for y in 0..64 {
        for z in -6i32..=6 {
            for x in -6i32..=6 {
                let edge = x.abs() == 6 || z.abs() == 6;
                if edge {
                    set_block(&plasmid, IVec3::new(x, y, z), &BEDROCK);
                } else {
                    if y % 5 == 0 {
                        set_block(&plasmid, IVec3::new(x, y, z), &SNOW_BLOCK);
                    }
                }
            }
        }
    }

    set_block(&plasmid, IVec3::new(0, 64, 0), &AIR);

    let mut known_participants = HashSet::new();

    loop {
        let participants = plasmid.send(GetParticipants).await.unwrap();
        for participant in participants {
            if known_participants.insert(participant) {
                give_item(&plasmid, participant, DIAMOND_SHOVEL);
            }
        }

        tokio::time::sleep(Duration::from_millis(1000)).await;
    }
}

fn set_block(plasmid: &Address<Plasmid>, pos: IVec3, block: &str) {
    plasmid.do_send(Command::SetBlock { pos, block: String::from(block) }).unwrap();
}

fn give_item(plasmid: &Address<Plasmid>, player: PlayerRef, item: &str) {
    plasmid.do_send(Command::GiveItem { player, item: String::from(item), quantity: 1 }).unwrap();
}
