package io.github.vishalsharma7nov.kmpproto

/**
 * Where `.proto` files are loaded from for `kmpProtoGenerate` / `syncProtos`.
 *
 * Set this on [ProtoClientConfig.protoSource]. When omitted (or
 * [ProtoSource.Local] with no path), generate looks for `.proto` files in
 * the project directory (`protos/`, `proto/`, `vendor/protos/`, then cwd).
 *
 * This field is not used at request time by `createApi` / `createClient`.
 */
public sealed class ProtoSource {
    /** Local directory. Omit [path] to discover protos in the project. */
    public data class Local(
        val path: String? = null,
    ) : ProtoSource()

    /** Clone [repo] at [ref]. Optional [path] is a subdirectory inside the repo. */
    public data class Github(
        val repo: String,
        val ref: String,
        val path: String? = null,
    ) : ProtoSource()

    /** Export [module] at [ref] via the Buf CLI. */
    public data class Buf(
        val module: String,
        val ref: String,
    ) : ProtoSource()

    public companion object {
        public fun local(path: String? = null): Local = Local(path)

        public fun github(repo: String, ref: String, path: String? = null): Github =
            Github(repo = repo, ref = ref, path = path)

        public fun buf(module: String, ref: String): Buf =
            Buf(module = module, ref = ref)
    }
}
