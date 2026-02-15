$blockNames = @(
"nsb_logo",
"nsb_sign",
"nsc_logo",
"nse_logo",
"nse_sign",
"nso_logo",
"nso_sign",
"nsr_logo",
"nsr_sign",
"nst_logo",
"big_concrete_brick",
"brushed_aluminum",
"cinder_brick_wall",
"coarse_concrete_floor",
"concrete_brick_wall",
"concrete_rail_tunnel_down",
"concrete_rail_tunnel_top",
"concrete_rail_tunnel_up",
"concrete_wall",
"corrugated_blue",
"corrugated_cyan",
"corrugated_green",
"corrugated_magenta",
"corrugated_orange",
"corrugated_purple",
"corrugated_red",
"corrugated_white",
"corrugated_yellow",
"dark_grey_cell_tile",
"dark_latex_painted_wall",
"dense_concrete_wall",
"dense_mesh_wire",
"exq_marble",
"grey_brick",
"latex_painted_wall",
"light_grey_cell_tile",
"marble",
"mesh_wire",
"mineral_wool_celling_plate",
"oblique_paving_brick",
"old_brick",
"plazza_floor",
"red_ko_mak",
"road_bed_stone",
"road_cyan_tile",
"road_dark_green_tile",
"road_dark_grey_tile",
"road_dark_orange_tile",
"road_dark_pink_tile",
"road_dark_yellow_tile",
"road_dust_yellow_tile",
"road_grey_tile",
"road_light_blue_tile",
"road_light_green_tile",
"road_light_grey_tile",
"road_light_orange_tile",
"road_light_pink_tile",
"road_light_yellow_tile",
"road_purple_tile",
"road_red_tile",
"road_shine_blue_tile",
"road_shine_orange_tile",
"road_shine_yellow_tile",
"road_sky_blue_tile",
"road_stone_yellow_tile",
"road_white_tile",
"road_yellow_tile",
"rusty_tread_steel_plate",
"sand_brick",
"square_iron_mesh",
"tarp_blue",
"tarp_green",
"tarp_magenta",
"tarp_orange",
"tarp_purple",
"tarp_red",
"tarp_yellow",
"tatami",
"tread_steel_black",
"tread_steel_blue",
"tread_steel_brown",
"tread_steel_cyan",
"tread_steel_green",
"tread_steel_grey",
"tread_steel_light_blue",
"tread_steel_light_grey",
"tread_steel_lime",
"tread_steel_magenta",
"tread_steel_orange",
"tread_steel_pink",
"tread_steel_purple",
"tread_steel_red",
"tread_steel_white",
"tread_steel_yellow",
"white_brick_wall",
"zry_bathroom_cell_tile",
"zry_tvbg_wall"
)

$basePath = "c:\Users\Administrator\Pictures\HydroNyaSama\common\src\main\resources\assets\hydronyasama"

foreach ($name in $blockNames) {
    # Blockstate
    $bsContent = @{
        "variants" = @{
            "" = @{
                "model" = "hydronyasama:block/$name"
            }
        }
    } | ConvertTo-Json -Depth 3
    Set-Content -Path "$basePath\blockstates\$name.json" -Value $bsContent

    # Block Model
    $bmContent = @{
        "parent" = "block/cube_all"
        "textures" = @{
            "all" = "hydronyasama:block/$name"
        }
    } | ConvertTo-Json -Depth 3
    Set-Content -Path "$basePath\models\block\$name.json" -Value $bmContent

    # Item Model
    $imContent = @{
        "parent" = "hydronyasama:block/$name"
    } | ConvertTo-Json -Depth 3
    Set-Content -Path "$basePath\models\item\$name.json" -Value $imContent
}
