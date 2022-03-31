use std::pin::Pin;
use anyhow::Error;
use futures::{Sink, SinkExt, Stream, StreamExt};
use tokio::net::TcpStream;
use serde::{Serialize, Deserialize};
use uuid::Uuid;
use glam::{IVec3, Vec3};
use bytes::Bytes;
use async_trait::async_trait;
use xtra::{Actor, Address, Context, Handler, Message, spawn::TokioGlobalSpawnExt};

const MAX_FRAME_LENGTH: usize = 4 * 1024 * 1024;
const FRAME_HEADER_SIZE: usize = 4;

#[derive(Serialize, Deserialize, Debug)]
#[serde(transparent)]
pub struct PlayerRef(Uuid);

pub type Identifier = String;

#[derive(Serialize)]
#[serde(tag = "type", content = "body")]
pub enum Command {
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

#[derive(Deserialize, Debug)]
#[serde(tag = "type", content = "body")]
pub enum Event {
    #[serde(rename = "plasmid:participants")]
    SetParticipants {
        players: Vec<PlayerRef>,
    },
}

impl Message for Event {
    type Result = ();
}

pub struct Plasmid {
    tx: Pin<Box<dyn Sink<Command, Error = Error> + Send + Sync + 'static>>,
    participants: Vec<PlayerRef>,
}

#[async_trait]
impl Actor for Plasmid {
    type Stop = ();
    async fn stopped(self) {}
}

#[async_trait]
impl Handler<Event> for Plasmid {
    async fn handle(&mut self, message: Event, _ctx: &mut Context<Self>) {
        dbg!(message);
    }
}

impl Plasmid {
    pub async fn connect() -> Address<Self> {
        let tcp = TcpStream::connect("localhost:12345").await.unwrap();
        let (tx, rx) = split_framed(tcp);

        let plasmid = Plasmid {
            tx: Box::pin(tx),
            participants: vec![],
        };

        let addr = plasmid.create(None).spawn_global();
        tokio::spawn(addr.clone().attach_stream(rx));
        addr
    }
}

fn split_framed(stream: TcpStream) -> (impl Sink<Command, Error = Error> + Send, impl Stream<Item = Event>) {
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
        serde_json::from_slice(&result.unwrap()).unwrap()
    });

    (sink, stream)
}