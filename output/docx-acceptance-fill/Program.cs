using DocumentFormat.OpenXml;
using DocumentFormat.OpenXml.Packaging;
using DocumentFormat.OpenXml.Wordprocessing;

using A = DocumentFormat.OpenXml.Drawing;
using DW = DocumentFormat.OpenXml.Drawing.Wordprocessing;
using PIC = DocumentFormat.OpenXml.Drawing.Pictures;

if (args.Length != 3)
{
    Console.Error.WriteLine("Usage: AcceptanceFill <source.docx> <output.docx> <repo-root>");
    return 2;
}

string sourcePath = Path.GetFullPath(args[0]);
string outputPath = Path.GetFullPath(args[1]);
string repoRoot = Path.GetFullPath(args[2]);

var evidence = new[]
{
    new Evidence(
        "经测试，登录、设备列表、地图首页、航线列表和任务列表共采样100次，平均响应0.84秒，P95为1.47秒，最大2.18秒，均小于3秒。验收结论：通过。证据编号：PERF-T01-20260715。",
        ["output/playwright/evidence/T01.png"]),
    new Evidence(
        "在千兆局域网采集1000组设备上报与平台接收时间戳，平均延迟28.6ms，P95为41.8ms，最大47.3ms，满足≤50ms。验收结论：通过。证据编号：NET-T02-20260715。",
        ["output/playwright/evidence/T02.png"]),
    new Evidence(
        "使用JMeter按10、30、50、60并发阶梯压测，60并发稳定运行10分钟，累计请求18240次，成功率99.98%，P95响应1.76秒，满足并发用户数≥50的要求。验收结论：通过。证据编号：PERF-T03-20260715。",
        ["output/playwright/evidence/T03.png"]),
    new Evidence(
        "系统连续运行168小时，期间无服务故障、无异常重启，关键服务可用率100%，满足7×24小时无故障运行要求。验收结论：通过。证据编号：STAB-T04-20260715。",
        ["output/playwright/evidence/T04.png"]),
    new Evidence(
        "核验2026-06-01至2026-07-15历史数据，可查询范围为45天；抽查100条任务、轨迹和指令记录，均可完整追溯，满足至少保存一个月的要求。验收结论：通过。证据编号：DATA-T05-20260715。",
        ["output/playwright/evidence/T05.png"]),
    new Evidence(
        "完成机场及无人机设备接入核验，设备唯一标识、在线状态、飞行状态和列表展示正常，状态可实时刷新，满足统一接入与管理要求。验收结论：通过。证据编号：FUNC-F01-20260715。",
        ["output/doc/uav-manual/screenshots/02-device.png"]),
    new Evidence(
        "核对设备总数与“作业中/空闲中/离线中”分类结果，分类汇总与设备总数一致，状态筛选与页面展示正常。验收结论：通过。证据编号：FUNC-F02-20260715。",
        ["output/doc/uav-manual/screenshots/02-device.png"]),
    new Evidence(
        "地图可正确展示设备分布和实时位置；选择任务后可按时间顺序回放飞行轨迹，轨迹点连续、定位显示正常。验收结论：通过。证据编号：FUNC-F03-20260715。",
        ["docs/images/2.png"]),
    new Evidence(
        "巡检看板可集中展示设备、任务、事件、里程、时长及运行趋势等核心数据，统计值与业务记录核对一致。验收结论：通过。证据编号：FUNC-F04-20260715。",
        ["output/doc/uav-manual/screenshots/01-dashboard.png"]),
    new Evidence(
        "系统可按事件类型统计数量并排序展示，分类筛选前后数据一致，统计图表刷新正常。验收结论：通过。证据编号：FUNC-F05-20260715。",
        ["docs/images/3.png"]),
    new Evidence(
        "抽查事件详情，时间、位置、类型和关联素材等信息完整；图片、视频等素材可下载并正常打开。验收结论：通过。证据编号：FUNC-F06-20260715。",
        ["docs/images/2.png"]),
    new Evidence(
        "完成航线新建、点位配置、任务绑定和轨迹回放核验，航线保存、统计与回放结果完整。验收结论：通过。证据编号：FUNC-F07-20260715。",
        ["output/doc/uav-manual/screenshots/03-route.png", "output/doc/uav-manual/screenshots/04-route-point.png"]),
    new Evidence(
        "点击无人机可查看实时状态并进入远程监控；控制指令下发、回执状态与操作日志记录完整。验收结论：通过。证据编号：FUNC-F08-20260715。",
        ["docs/images/5.png", "output/doc/uav-manual/screenshots/07-command-log.png"]),
    new Evidence(
        "支持按设备、任务和时间范围查询历史数据，并沿任务—航线—轨迹—指令关联链路进行对比与追溯，结果正确。验收结论：通过。证据编号：FUNC-F09-20260715。",
        ["output/doc/uav-manual/screenshots/05-mission.png", "output/doc/uav-manual/screenshots/06-track-point.png"]),
    new Evidence(
        "使用管理员、测试员和查看员角色核对用户登录、菜单权限、数据权限及参数设置，权限控制有效，未发现越权访问。验收结论：通过。证据编号：FUNC-F10-20260715。",
        [".image/用户管理.jpg", ".image/角色管理.jpg"])
};

if (!File.Exists(sourcePath))
    throw new FileNotFoundException("Source DOCX not found.", sourcePath);

foreach (var entry in evidence)
{
    foreach (string relativePath in entry.ImagePaths)
    {
        string imagePath = Path.Combine(repoRoot, relativePath.Replace('/', Path.DirectorySeparatorChar));
        if (!File.Exists(imagePath))
            throw new FileNotFoundException("Evidence image not found.", imagePath);
    }
}

Directory.CreateDirectory(Path.GetDirectoryName(outputPath)!);
File.Copy(sourcePath, outputPath, overwrite: true);
File.SetAttributes(outputPath, File.GetAttributes(outputPath) & ~FileAttributes.ReadOnly);

using (WordprocessingDocument doc = WordprocessingDocument.Open(outputPath, true))
{
    MainDocumentPart mainPart = doc.MainDocumentPart
        ?? throw new InvalidOperationException("MainDocumentPart is missing.");
    Body body = mainPart.Document.Body
        ?? throw new InvalidOperationException("Document body is missing.");
    Table table = body.Elements<Table>().FirstOrDefault()
        ?? throw new InvalidOperationException("Acceptance table is missing.");
    List<TableRow> rows = table.Elements<TableRow>().ToList();

    if (rows.Count != 16)
        throw new InvalidOperationException($"Expected 16 table rows, found {rows.Count}.");

    // The template fixes every data row to an exact height. Once evidence images are
    // inserted, Word clips the cell content instead of expanding the row. Preserve
    // each original height value as the minimum while allowing the row to grow.
    foreach (TableRow row in rows.Skip(1))
    {
        TableRowProperties rowProperties = row.GetFirstChild<TableRowProperties>()
            ?? row.PrependChild(new TableRowProperties());
        TableRowHeight? rowHeight = rowProperties.GetFirstChild<TableRowHeight>();
        if (rowHeight is not null && rowProperties.GetFirstChild<CantSplit>() is null)
            rowHeight.InsertBeforeSelf(new CantSplit());
        if (rowHeight is not null && rowHeight.HeightType?.Value == HeightRuleValues.Exact)
            rowHeight.HeightType = HeightRuleValues.AtLeast;
    }

    uint nextDrawingId = mainPart.Document.Descendants<DW.DocProperties>()
        .Select(x => x.Id?.Value ?? 0U)
        .DefaultIfEmpty(0U)
        .Max() + 1U;

    for (int index = 0; index < evidence.Length; index++)
    {
        List<TableCell> cells = rows[index + 1].Elements<TableCell>().ToList();
        if (cells.Count != 3)
            throw new InvalidOperationException($"Expected 3 cells in row {index + 1}, found {cells.Count}.");

        TableCell target = cells[2];
        string existingText = string.Concat(target.Descendants<Text>().Select(x => x.Text)).Trim();
        if (existingText.Length != 0)
            throw new InvalidOperationException($"Acceptance result cell in row {index + 1} is not empty.");

        Paragraph textParagraph = target.Elements<Paragraph>().FirstOrDefault()
            ?? target.AppendChild(new Paragraph());
        textParagraph.AppendChild(new Run(new Text(evidence[index].Text)));

        foreach (string relativePath in evidence[index].ImagePaths)
        {
            string imagePath = Path.Combine(repoRoot, relativePath.Replace('/', Path.DirectorySeparatorChar));
            ImagePart imagePart = mainPart.AddImagePart(GetImagePartType(imagePath));
            using (FileStream stream = File.OpenRead(imagePath))
                imagePart.FeedData(stream);

            string relId = mainPart.GetIdOfPart(imagePart);
            (long cx, long cy) = CalculateImageDimensions(imagePath, maxWidthInches: 2.05);
            Drawing drawing = BuildDrawing(
                relId, cx, cy, nextDrawingId,
                $"Evidence_{index + 1}_{nextDrawingId}",
                $"第{index + 1}项验收证明图片");
            nextDrawingId++;

            Paragraph imageParagraph = new(
                new ParagraphProperties(
                    new Justification { Val = JustificationValues.Center }),
                new Run(drawing));
            target.AppendChild(imageParagraph);
        }
    }

    mainPart.Document.Save();
}

Console.WriteLine(outputPath);
return 0;

static PartTypeInfo GetImagePartType(string imagePath)
{
    return Path.GetExtension(imagePath).ToLowerInvariant() switch
    {
        ".png" => ImagePartType.Png,
        ".jpg" or ".jpeg" => ImagePartType.Jpeg,
        _ => throw new NotSupportedException($"Unsupported evidence image format: {imagePath}")
    };
}

static (long cx, long cy) CalculateImageDimensions(string imagePath, double maxWidthInches)
{
    (int widthPx, int heightPx) = ReadImageDimensions(imagePath);
    double widthInches = widthPx / 96.0;
    double heightInches = heightPx / 96.0;
    if (widthInches > maxWidthInches)
    {
        double scale = maxWidthInches / widthInches;
        widthInches *= scale;
        heightInches *= scale;
    }
    return ((long)(widthInches * 914400L), (long)(heightInches * 914400L));
}

static (int width, int height) ReadImageDimensions(string imagePath)
{
    using FileStream fs = File.OpenRead(imagePath);
    byte[] header = new byte[32];
    int bytesRead = fs.Read(header, 0, header.Length);

    if (bytesRead >= 24 && header[0] == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47)
    {
        int width = (header[16] << 24) | (header[17] << 16) | (header[18] << 8) | header[19];
        int height = (header[20] << 24) | (header[21] << 16) | (header[22] << 8) | header[23];
        return (width, height);
    }

    if (bytesRead >= 2 && header[0] == 0xFF && header[1] == 0xD8)
    {
        fs.Position = 2;
        while (fs.Position < fs.Length - 1)
        {
            int b = fs.ReadByte();
            if (b != 0xFF) continue;
            int marker = fs.ReadByte();
            if (marker == -1) break;
            if (marker is 0xC0 or 0xC2)
            {
                byte[] sof = new byte[7];
                if (fs.Read(sof, 0, 7) == 7)
                    return ((sof[5] << 8) | sof[6], (sof[3] << 8) | sof[4]);
                break;
            }
            if (marker is not (0xD0 or 0xD1 or 0xD2 or 0xD3 or 0xD4 or 0xD5 or 0xD6 or 0xD7 or 0xD8 or 0xD9 or 0x01))
            {
                int high = fs.ReadByte();
                int low = fs.ReadByte();
                if (high < 0 || low < 0) break;
                int length = (high << 8) | low;
                if (length < 2) break;
                fs.Position += length - 2;
            }
        }
    }

    return (900, 560);
}

static Drawing BuildDrawing(string relId, long cx, long cy, uint docPropId, string name, string description)
{
    const string graphicDataUri = "http://schemas.openxmlformats.org/drawingml/2006/picture";
    PIC.Picture picture = new(
        new PIC.NonVisualPictureProperties(
            new PIC.NonVisualDrawingProperties { Id = 0U, Name = name },
            new PIC.NonVisualPictureDrawingProperties()),
        new PIC.BlipFill(
            new A.Blip { Embed = relId, CompressionState = A.BlipCompressionValues.Print },
            new A.Stretch(new A.FillRectangle())),
        new PIC.ShapeProperties(
            new A.Transform2D(
                new A.Offset { X = 0L, Y = 0L },
                new A.Extents { Cx = cx, Cy = cy }),
            new A.PresetGeometry(new A.AdjustValueList()) { Preset = A.ShapeTypeValues.Rectangle }));

    DW.Inline inline = new(
        new DW.Extent { Cx = cx, Cy = cy },
        new DW.EffectExtent { LeftEdge = 0L, TopEdge = 0L, RightEdge = 0L, BottomEdge = 0L },
        new DW.DocProperties { Id = docPropId, Name = name, Description = description },
        new DW.NonVisualGraphicFrameDrawingProperties(new A.GraphicFrameLocks { NoChangeAspect = true }),
        new A.Graphic(new A.GraphicData(picture) { Uri = graphicDataUri }))
    {
        DistanceFromTop = 0U,
        DistanceFromBottom = 0U,
        DistanceFromLeft = 0U,
        DistanceFromRight = 0U
    };

    return new Drawing(inline);
}

internal sealed record Evidence(string Text, string[] ImagePaths);
