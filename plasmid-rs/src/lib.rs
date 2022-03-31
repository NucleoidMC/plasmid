use anyhow::Error;
use futures::{Sink, SinkExt, Stream, StreamExt};
use tokio::net::TcpStream;
use serde::{Serialize, Deserialize};
use uuid::Uuid;
use glam::{IVec3, Vec3};
use bytes::Bytes;

const MAX_FRAME_LENGTH: usize = 4 * 1024 * 1024;
const FRAME_HEADER_SIZE: usize = 4;

#[derive(Serialize, Deserialize)]
#[serde(transparent)]
pub struct PlayerRef(Uuid);

pub type Identifier = String;

#[derive(Serialize)]
#[serde(tag = "type", content = "body")]
pub enum Command {
    #[serde(rename = "plasmid:get_participants")]
    GetParticipants,
    #[serde(rename = "plasmid:teleport")]
    Teleport {
        player: PlayerRef,
        dest: Vec3,
    },
    #[serde(rename = "plasmid:set_block")]
    SetBlock {
        pos: IVec3,
        block: Identifier,
    },
    #[serde(rename = "plasmid:give_item")]
    GiveItem {
        player: PlayerRef,
        item: Identifier,
        quantity: u32,
    }
}

fn split_framed(stream: TcpStream) -> (impl Sink<Command, Error = Error> + Send, impl Stream<Item = String>) {
    let (sink, stream) = tokio_util::codec::LengthDelimitedCodec::builder()
        .big_endian()
        .max_frame_length(MAX_FRAME_LENGTH)
        .length_field_length(FRAME_HEADER_SIZE)
        .num_skip(FRAME_HEADER_SIZE)
        .length_field_offset(0)
        .length_adjustment(0)
        .new_framed(stream)
        .split();

    let sink = sink.with(|message: Command| async move {
        let mut bytes = Vec::with_capacity(64);
        serde_json::to_writer(&mut bytes, &message)?;
        Ok(Bytes::from(bytes))
    });

    let stream = stream.map(|result| {
        String::from_utf8(result.unwrap().to_vec()).unwrap()
    });

    (sink, stream)
}